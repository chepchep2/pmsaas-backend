package com.chep.demo.todo.service.task;

import com.chep.demo.todo.domain.task.Task;
import com.chep.demo.todo.domain.task.TaskRepository;
import com.chep.demo.todo.domain.user.User;
import com.chep.demo.todo.domain.user.UserRepository;
import com.chep.demo.todo.dto.task.CreateTaskRequest;
import com.chep.demo.todo.dto.task.MoveTaskRequest;
import com.chep.demo.todo.dto.task.UpdateAssigneesRequest;
import com.chep.demo.todo.dto.task.UpdateDueDateRequest;
import com.chep.demo.todo.dto.task.UpdateTaskRequest;
import com.chep.demo.todo.exception.auth.AuthenticationException;
import com.chep.demo.todo.exception.task.TaskNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Task> getTasks(Long userId) {
        return taskRepository.findAllByUserIdOrderByOrderIndexAsc(userId);
    }

    public Task createTask(Long userId, CreateTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        Integer orderIndex = request.orderIndex();
        Long totalCounting = taskRepository.countByUserId(userId);
        int totalCount = totalCounting.intValue();

        if (orderIndex == null) {
            orderIndex = totalCount;
        } else {
            // 0 ~ totalCount 사이만 허용
            if (orderIndex < 0 || orderIndex > totalCount) {
                throw new IllegalArgumentException("orderIndex out of range: 0 ~ " + totalCount);
            }

            if (orderIndex < totalCount) {
                List<Task> affectedTasks = taskRepository.findByUserIdAndOrderIndexBetween(userId, orderIndex, totalCount - 1);

                shiftOrderIndexRange(affectedTasks, + 1);

                taskRepository.saveAll(affectedTasks);
            }
        }

        Set<User> assignees = resolveAssignees(request.assigneeIds());

        Task task = Task.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .orderIndex(orderIndex)
                .dueDate(request.dueDate())
                .build();

        task.changeAssignees(assignees);

        return taskRepository.save(task);
    }

    private void shiftOrderIndexRange(List<Task> affectedTasks, int delta) {
        for (Task affected: affectedTasks) {
            affected.changeOrderIndex(affected.getOrderIndex() + delta);
        }
    }

    private Set<User> resolveAssignees(List<Long> assigneeIds) {
        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return new HashSet<>();
        }

        List<User> users = userRepository.findAllById(assigneeIds);
        if (users.size() != new HashSet<>(assigneeIds).size()) {
            throw new IllegalArgumentException("Invalid assignee id provided");
        }
        return new HashSet<>(users);
    }

    public Task updateTask(Long userId, Long taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        task.changeTitleAndContent(request.title(), request.content());

        return taskRepository.save(task);
    }

    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        int deletedOrderIndex = task.getOrderIndex();

        taskRepository.softDelete(task);

        List<Task> affectedTasks = taskRepository.findByUserIdAndOrderIndexGreaterThan(userId, deletedOrderIndex);

        shiftOrderIndexRange(affectedTasks, - 1);

        taskRepository.saveAll(affectedTasks);
    }

    public void toggleTaskComplete(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        task.toggleComplete();

        taskRepository.save(task);
    }

    public void move(Long userId, Long taskId, MoveTaskRequest request) {
        Task target = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Integer targetOrderIndex = request.targetOrderIndex();
        Integer currentOrderIndex = target.getOrderIndex();

        if (targetOrderIndex.equals(currentOrderIndex)) {
            return;
        }

        int maxIndex = taskRepository.countByUserId(userId).intValue() - 1;
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

        List<Task> affectedTasks = taskRepository.findByUserIdAndOrderIndexBetween(userId, start, end);

        List<Task> changedTasks = Task.reorder(target, targetOrderIndex, affectedTasks);

        if (!changedTasks.isEmpty()) {
            taskRepository.saveAll(changedTasks);
        }
    }

    public Task updateAssignees(Long userId, Long taskId, UpdateAssigneesRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        task.changeAssignees(resolveAssignees(request.assigneeIds()));
        return taskRepository.save(task);
    }

    public Task updateDueDate(Long userId, Long taskId, UpdateDueDateRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        task.changeDueDate((request.dueDate()));
        return taskRepository.save(task);
    }
}
