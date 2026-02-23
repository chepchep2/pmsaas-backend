package com.chep.demo.todo.controller.notification;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.dto.notification.NotificationItemResponse;
import com.chep.demo.todo.dto.notification.NotificationResponse;
import com.chep.demo.todo.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @GetMapping("/api/notifications/me")
    ResponseEntity<NotificationResponse> getUserNotifications(
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) Instant cursorCreatedAt,
            @RequestParam(required = false) Long cursorNotificationId,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = currentUserId();
        NotificationResponse response = notificationService.getUserNotifications(userId, workspaceId, cursorCreatedAt, cursorNotificationId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/workspaces/{workspaceId}/notifications")
    ResponseEntity<NotificationResponse> getWorkspaceNotifications(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Instant cursorCreatedAt,
            @RequestParam(required = false) Long cursorNotificationId,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = currentUserId();
        NotificationResponse response = notificationService.getWorkspaceNotifications(userId, workspaceId, cursorCreatedAt, cursorNotificationId, limit);
        return ResponseEntity.ok(response);
    }

    private NotificationItemResponse toResponse(Notification notification) {
        return new NotificationItemResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTask().getTitle(),
                notification.getCreatedAt()
        );
    }
}
