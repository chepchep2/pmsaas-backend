package com.chep.demo.todo.service.task;

import com.chep.demo.todo.domain.project.Project;
import com.chep.demo.todo.domain.project.ProjectRepository;
import com.chep.demo.todo.domain.task.Task;
import com.chep.demo.todo.domain.task.TaskRepository;
import com.chep.demo.todo.domain.user.User;
import com.chep.demo.todo.domain.user.UserRepository;
import com.chep.demo.todo.domain.workspace.Workspace;
import com.chep.demo.todo.domain.workspace.WorkspaceMember;
import com.chep.demo.todo.domain.workspace.WorkspaceMemberRepository;
import com.chep.demo.todo.domain.workspace.WorkspaceRepository;
import com.chep.demo.todo.dto.task.CreateTaskRequest;
import com.chep.demo.todo.dto.task.MoveTaskRequest;
import com.chep.demo.todo.dto.task.UpdateAssigneesRequest;
import com.chep.demo.todo.dto.task.UpdateDueDateRequest;
import com.chep.demo.todo.dto.task.UpdateTaskRequest;
import com.chep.demo.todo.exception.auth.AuthenticationException;
import com.chep.demo.todo.exception.project.ProjectNotFoundException;
import com.chep.demo.todo.exception.task.TaskNotFoundException;
import com.chep.demo.todo.exception.workspace.WorkspaceAccessDeniedException;
import com.chep.demo.todo.exception.workspace.WorkspaceNotFoundException;
import com.chep.demo.todo.service.notification.event.TaskAssigneesChangedEvent;
import com.chep.demo.todo.service.notification.event.TaskCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            ProjectRepository projectRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksForUser(Long userId) {
        return taskRepository.findAllByUserIdOrderByOrderIndexAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<Task> getProjectTasks(Long workspaceId, Long projectId, Long userId) {
        validateWorkspaceMember(workspaceId, userId);
        validateWorkspaceProject(workspaceId, projectId);
        return taskRepository.findAllByProjectIdOrderByOrderIndexAsc(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> getWorkspaceTasks(Long workspaceId, Long userId) {
        validateWorkspaceMember(workspaceId, userId);
        return taskRepository.findAllByWorkspaceId(workspaceId);
    }

    public Task createTask(Long workspaceId, Long userId, CreateTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
        validateWorkspaceMember(workspaceId, userId);

        Project project;
        if (request.projectId() == null) {
            project = projectRepository.findByWorkspaceIdAndDefaultProject(workspaceId, true)
                    .orElseThrow(() -> new ProjectNotFoundException("Default project not found"));
        } else {
            project = projectRepository.findByIdAndWorkspaceId(request.projectId(), workspaceId)
                    .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        }

        Integer orderIndex = request.orderIndex();
        Long totalCounting = taskRepository.countByProjectId(project.getId());
        int totalCount = totalCounting.intValue();

        if (orderIndex == null) {
            orderIndex = totalCount;
        } else {
            // 0 ~ totalCount 사이만 허용
            if (orderIndex < 0 || orderIndex > totalCount) {
                throw new IllegalArgumentException("orderIndex out of range: 0 ~ " + totalCount);
            }

            if (orderIndex < totalCount) {
                List<Task> affectedTasks = taskRepository.findByProjectIdAndOrderIndexBetween(project.getId(), orderIndex, totalCount - 1);

                shiftOrderIndexRange(affectedTasks, + 1);

                taskRepository.saveAll(affectedTasks);
            }
        }

        Set<User> assignees = resolveAssignees(workspaceId, request.assigneeIds());

        Task task = Task.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .orderIndex(orderIndex)
                .dueDate(request.dueDate())
                .project(project)
                .build();
        task.changeAssignees(assignees);
        Task savedTask = taskRepository.save(task);

        applicationEventPublisher.publishEvent(new TaskCreatedEvent(workspaceId, savedTask.getId()));

        return savedTask;
    }

    private void shiftOrderIndexRange(List<Task> affectedTasks, int delta) {
        for (Task affected: affectedTasks) {
            affected.changeOrderIndex(affected.getOrderIndex() + delta);
        }
    }

    private Set<User> resolveAssignees(Long workspaceId, List<Long> assigneeIds) {
        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return new HashSet<>();
        }

        List<User> users = userRepository.findAllById(assigneeIds);
        if (users.size() != new HashSet<>(assigneeIds).size()) {
            throw new IllegalArgumentException("Invalid assignee id provided");
        }
        for (User user : users) {
            validateWorkspaceMember(workspaceId, user.getId());
        }
        return new HashSet<>(users);
    }

    public Task updateTask(Long userId, Long taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        validateWorkspaceMember(task.getProject().getWorkspace().getId(), userId);
        task.changeTitleAndContent(request.title(), request.content());

        return taskRepository.save(task);
    }

    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        validateWorkspaceMember(task.getProject().getWorkspace().getId(), userId);
        Long projectId = task.getProject().getId();

        int deletedOrderIndex = task.getOrderIndex();

        taskRepository.softDelete(task);

        List<Task> affectedTasks = taskRepository.findByProjectIdAndOrderIndexGreaterThan(projectId, deletedOrderIndex);

        shiftOrderIndexRange(affectedTasks, - 1);

        taskRepository.saveAll(affectedTasks);
    }

    public void toggleTaskComplete(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        validateWorkspaceMember(task.getProject().getWorkspace().getId(), userId);
        task.toggleComplete();

        taskRepository.save(task);
    }

    public void move(Long userId, Long taskId, MoveTaskRequest request) {
        Task target = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        validateWorkspaceMember(target.getProject().getWorkspace().getId(), userId);
        Long projectId = target.getProject().getId();

        Integer targetOrderIndex = request.targetOrderIndex();
        Integer currentOrderIndex = target.getOrderIndex();

        if (targetOrderIndex.equals(currentOrderIndex)) {
            return;
        }

        int maxIndex = taskRepository.countByProjectId(projectId).intValue() - 1;
        if (targetOrderIndex > maxIndex) {
            throw new IllegalArgumentException("targetIndex exceeds maximum");
        }

        int start;
        int end;

        if (targetOrderIndex < currentOrderIndex) {
            start = targetOrderIndex;
            end = currentOrderIndex - 1;
        } else {
            start = currentOrderIndex + 1;
            end = targetOrderIndex;
        }

        List<Task> affectedTasks = taskRepository.findByProjectIdAndOrderIndexBetween(projectId, start, end);

        List<Task> changedTasks = Task.reorder(target, targetOrderIndex, affectedTasks);

        if (!changedTasks.isEmpty()) {
            taskRepository.saveAll(changedTasks);
        }
    }

    public Task updateAssignees(Long userId, Long taskId, UpdateAssigneesRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        validateWorkspaceMember(task.getProject().getWorkspace().getId(), userId);
        Long workspaceId = task.getProject().getWorkspace().getId();

        Set<Long> prevIds = task.getAssignees().stream()
                        .map(t -> t.getUser().getId())
                        .collect(Collectors.toSet());

        Set<User> newAssignees = resolveAssignees(workspaceId, request.assigneeIds());
        Set<Long> newIds = newAssignees.stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());

        if (prevIds.equals(newIds)) {
            return task;
        }

        task.changeAssignees(newAssignees);
        Task savedTask = taskRepository.save(task);

        Set<Long> added = new HashSet<>(newIds);
        added.removeAll(prevIds);

        applicationEventPublisher.publishEvent(new TaskAssigneesChangedEvent(savedTask.getId(), userId, new ArrayList<>(added)));

        return savedTask;
    }

    public Task updateDueDate(Long userId, Long taskId, UpdateDueDateRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        task.changeDueDate((request.dueDate()));
        return taskRepository.save(task);
    }

    private void validateWorkspaceMember(Long workspaceId, Long userId) {
        workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, WorkspaceMember.Status.ACTIVE)
                .orElseThrow(() -> new WorkspaceAccessDeniedException("Access denied to this workspace"));
    }

    private void validateWorkspaceProject(Long workspaceId, Long projectId) {
        projectRepository.findByIdAndWorkspaceId(projectId, workspaceId).orElseThrow(() -> new ProjectNotFoundException("Project not found"));
    }
}
