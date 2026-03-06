package com.chep.demo.todo.service.notification.processor;

import com.chep.demo.todo.service.notification.NotificationStateService;
import com.chep.demo.todo.service.notification.SlackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationProcessor {
    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);
    private final NotificationStateService notificationStateService;
    private final SlackService slackService;

    public NotificationProcessor(NotificationStateService notificationStateService, SlackService slackService) {
        this.notificationStateService = notificationStateService;
        this.slackService = slackService;
    }

    public void process(Long notificationId) {
        boolean locked = notificationStateService.tryMarkSending(notificationId);

        if (!locked) {
            log.warn("Already processed or not found: {}", notificationId);
            return;
        }
        log.info("Marked as SENDING: notification={}", notificationId);
        slackService.sendNotification(notificationId);
    }
}
