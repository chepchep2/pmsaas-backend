package com.chep.demo.todo.domain.workspace;

import com.chep.demo.todo.domain.notification.Notification;

import java.time.Instant;
import java.util.List;

public interface NotificationQueryRepository {
    List<Notification> findUserNotifications(Long userId, Long workspaceId, Instant cursorCreatedAt, Long cursorId, int limit);
    List<Notification> findWorkspaceNotifications(Long workspaceId, Instant cursorCreatedAt, Long cursorId, int limit);
}