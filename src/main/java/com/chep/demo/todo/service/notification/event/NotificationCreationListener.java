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
import com.chep.demo.todo.domain.workspace.Workspace;
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

    public NotificationCreationListener(
            NotificationRepository notificationRepository,
            TaskRepository taskRepository,
            ApplicationEventPublisher applicationEventPublisher,
            Clock clock
    ) {
        this.notificationRepository = notificationRepository;
        this.taskRepository = taskRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock;
    }

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
}
