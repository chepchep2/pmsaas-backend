package com.chep.demo.todo.dto.notification;

import java.util.List;

public record NotificationResponse(
        List<NotificationItemResponse> notifications,
        boolean hasNext,
        String nextCursor
) {
}
