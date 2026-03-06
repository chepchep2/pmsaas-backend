package com.chep.demo.todo.dto.notification;

import java.time.Instant;
import java.util.List;

public record NotificationResponse(
        List<NotificationItemResponse> notifications,
        boolean hasNext,
        Instant nextCursorCreatedAt,
        Long nextCursorId
) {
}
