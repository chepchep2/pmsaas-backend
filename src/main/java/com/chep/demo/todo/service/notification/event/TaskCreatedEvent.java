package com.chep.demo.todo.service.notification.event;

public record TaskCreatedEvent(Long workspaceId, Long taskId) {
}
