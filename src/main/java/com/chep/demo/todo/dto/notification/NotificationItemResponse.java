package com.chep.demo.todo.dto.notification;

import java.time.Instant;

public record NotificationItemResponse(
        Long id,
        String type,
        String taskTitle,
        Instant createdAt
) {
}
