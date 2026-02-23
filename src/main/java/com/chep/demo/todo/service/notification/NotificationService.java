package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.workspace.Workspace;
import com.chep.demo.todo.domain.workspace.WorkspaceRepository;
import com.chep.demo.todo.dto.notification.NotificationItemResponse;
import com.chep.demo.todo.dto.notification.NotificationResponse;
import com.chep.demo.todo.exception.workspace.WorkspaceNotFoundException;
import com.chep.demo.todo.infrastructure.persistence.notification.NotificationQueryRepositoryImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class NotificationService {
    private final WorkspaceRepository workspaceRepository;
    private final NotificationQueryRepositoryImpl notificationQueryRepository;

    public NotificationService(
            WorkspaceRepository workspaceRepository,
            NotificationQueryRepositoryImpl notificationQueryRepository
    ) {
        this.workspaceRepository = workspaceRepository;
        this.notificationQueryRepository = notificationQueryRepository;
    }

    @Transactional(readOnly = true)
    public NotificationResponse getUserNotifications(Long userId, Long workspaceId, Instant cursorCreatedAt, Long cursorNotificationId, int limit) {
        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
            workspace.requireActiveMember(userId);
        }

        List<Notification> notifications = notificationQueryRepository.findUserNotifications(
                userId, workspaceId, cursorCreatedAt, cursorNotificationId, limit + 1);

        boolean hasNext = notifications.size() > limit;
        List<Notification> result = hasNext ? notifications.subList(0, limit) : notifications;

        Instant nextCursorCreatedAt = null;
        Long nextCursorId = null;

        if (hasNext && !result.isEmpty()) {
            Notification last = result.getLast();
            nextCursorCreatedAt = last.getCreatedAt();
            nextCursorId = last.getId();
        }

        List<NotificationItemResponse> mapped = result.stream()
                .map(n -> new NotificationItemResponse(
                        n.getId(),
                        n.getType().name(),
                        n.getTask().getTitle(),
                        n.getCreatedAt()
                )).toList();

        return new NotificationResponse(mapped, hasNext, nextCursorCreatedAt, nextCursorId);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getWorkspaceNotifications(Long userId, Long workspaceId, Instant cursorCreatedAt, Long cursorNotificationId, int limit) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
        workspace.requireOwnerMember(userId);

        List<Notification> notifications = notificationQueryRepository.findWorkspaceNotifications(workspaceId, cursorCreatedAt, cursorNotificationId, limit + 1);

        boolean hasNext = notifications.size() > limit;
        List<Notification> result = hasNext ? notifications.subList(0, limit) : notifications;

        Instant nextCursorCreatedAt = null;
        Long nextCursorId = null;

        if (hasNext && !result.isEmpty()) {
            Notification last = result.getLast();
            nextCursorCreatedAt = last.getCreatedAt();
            nextCursorId = last.getId();
        }

        List<NotificationItemResponse> mapped = result.stream()
                .map(n -> new NotificationItemResponse(
                        n.getId(),
                        n.getType().name(),
                        n.getTask().getTitle(),
                        n.getCreatedAt()
                )).toList();

        return new NotificationResponse(mapped, hasNext, nextCursorCreatedAt, nextCursorId);
    }
}