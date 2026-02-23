package com.chep.demo.todo.service.notification.event;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.notification.NotificationRepository;
import com.chep.demo.todo.domain.notification.NotificationType;
import com.chep.demo.todo.domain.notification.RecipientType;
import com.chep.demo.todo.domain.project.Project;
import com.chep.demo.todo.domain.task.Task;
import com.chep.demo.todo.domain.task.TaskAssignee;
import com.chep.demo.todo.domain.task.TaskRepository;
import com.chep.demo.todo.domain.user.User;
import com.chep.demo.todo.domain.user.UserRepository;
import com.chep.demo.todo.domain.workspace.Workspace;
import com.chep.demo.todo.exception.auth.UserNotFoundException;
import com.chep.demo.todo.exception.task.TaskNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class NotificationCreationListener {
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;
    private final UserRepository userRepository;

    public NotificationCreationListener(
            NotificationRepository notificationRepository,
            TaskRepository taskRepository,
            ApplicationEventPublisher applicationEventPublisher,
            Clock clock,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.taskRepository = taskRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock;
        this.userRepository = userRepository;
    }

    // WorkspaceNotificationsCreatedEvent는 NotificationSlackEventListener에서 @TransactionalEventListener(AFTER_COMMIT)으로 처리
    // 이 메서드의 @Transactional을 제거하면 이벤트 체인이 동작하지 않음.
    @Transactional
    @EventListener
    public void handleTaskCreated(TaskCreatedEvent event) {
        Task task = taskRepository.findByIdWithDetails(event.taskId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Workspace workspace = task.getProject().getWorkspace();
        Project project = task.getProject();
        User actor = task.getUser();
        Set<TaskAssignee> assignees = task.getAssignees();
        Instant now = Instant.now(clock);
        List<Notification> notifications = new ArrayList<>();

        notifications.add(Notification.forWorkspace(
                NotificationType.TASK_CREATED,
                workspace,
                task,
                actor,
                project,
                now
        ));

        for (TaskAssignee assignee : assignees) {
            notifications.add(Notification.forUser(
                    NotificationType.TASK_CREATED,
                    assignee.getUser().getId(),
                    workspace,
                    task,
                    actor,
                    project,
                    now
            ));
        }

        List<Notification> saved = notificationRepository.saveAll(notifications);

        List<Long> workspaceNotiIds = saved.stream()
                .filter(n -> n.getRecipientType() == RecipientType.WORKSPACE)
                .map(Notification::getId)
                .toList();

        applicationEventPublisher.publishEvent(new WorkspaceNotificationsCreatedEvent(event.workspaceId(), workspaceNotiIds));
    }

    // WorkspaceNotificationsCreatedEvent는 NotificationSlackEventListener에서 @TransactionalEventListener(AFTER_COMMIT)으로 처리
    // 이 메서드의 @Transactional을 제거하면 이벤트 체인이 동작하지 않음.
    @Transactional
    @EventListener
    public void handleTaskAssigneesChanged(TaskAssigneesChangedEvent event) {
        Task task = taskRepository.findByIdWithDetails(event.taskId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Workspace workspace = task.getProject().getWorkspace();
        Project project = task.getProject();

        User actor = userRepository.findById(event.actorId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Instant now = Instant.now(clock);

        List<Notification> notifications = new ArrayList<>();

        notifications.add(Notification.forWorkspace(
                NotificationType.TASK_ASSIGNEES_CHANGED,
                workspace,
                task,
                actor,
                project,
                now
        ));

        for (Long addedUserId : event.addedAssigneeIds()) {
            notifications.add(Notification.forUser(
                    NotificationType.TASK_ASSIGNEES_CHANGED,
                    addedUserId,
                    workspace,
                    task,
                    actor,
                    project,
                    now
            ));
        }

        List<Notification> saved = notificationRepository.saveAll(notifications);

        List<Long> workspaceNotiIds = saved.stream()
                .filter(n -> n.getRecipientType() == RecipientType.WORKSPACE)
                .map(Notification::getId)
                .toList();

        applicationEventPublisher.publishEvent(new WorkspaceNotificationsCreatedEvent(workspace.getId(), workspaceNotiIds));
    }
}
