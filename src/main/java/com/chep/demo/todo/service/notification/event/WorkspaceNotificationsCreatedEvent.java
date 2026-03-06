package com.chep.demo.todo.service.notification.event;

import java.util.List;

public record WorkspaceNotificationsCreatedEvent(Long workspaceId, List<Long> notificationIds) {
}
