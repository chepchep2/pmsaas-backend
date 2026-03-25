package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.common.cursor.CursorTokenUtils;
import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.notification.NotificationQueryRepository;
import com.chep.demo.todo.domain.workspace.Workspace;
import com.chep.demo.todo.domain.workspace.WorkspaceRepository;
import com.chep.demo.todo.dto.notification.NotificationItemResponse;
import com.chep.demo.todo.dto.notification.NotificationResponse;
import com.chep.demo.todo.exception.workspace.WorkspaceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationService {
    private final WorkspaceRepository workspaceRepository;
    private final NotificationQueryRepository notificationQueryRepository;

    public NotificationService(
            WorkspaceRepository workspaceRepository,
            NotificationQueryRepository notificationQueryRepository
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

        return buildResponse(notifications, limit);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getWorkspaceNotifications(Long userId, Long workspaceId, Instant cursorCreatedAt, Long cursorNotificationId, int limit) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
        workspace.requireOwnerMember(userId);

        List<Notification> notifications = notificationQueryRepository.findWorkspaceNotifications(workspaceId, cursorCreatedAt, cursorNotificationId, limit + 1);

        return buildResponse(notifications, limit);
    }

    private NotificationResponse buildResponse(List<Notification> notifications, int limit) {
        boolean hasNext = notifications.size() > limit;
        List<Notification> result = hasNext ? notifications.subList(0, limit) : notifications;

        String nextCursor = null;

        if (hasNext && !result.isEmpty()) {
            Notification last = result.getLast();
            nextCursor = CursorTokenUtils.encode(Map.of("createdAt", last.getCreatedAt().toString(), "id", last.getId()));
        }

        List<NotificationItemResponse> mapped = result.stream()
                .map(n -> new NotificationItemResponse(
                        n.getId(),
                        n.getType().name(),
                        n.getTask().getTitle(),
                        n.getCreatedAt()
                )).toList();

        return new NotificationResponse(mapped, hasNext, nextCursor);
    }
}
