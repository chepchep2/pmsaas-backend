package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.domain.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class SlackService {
    private static final Logger log = LoggerFactory.getLogger(SlackService.class);
    private final NotificationStateService notificationStateService;
    private final SlackClient slackClient;
    private final Clock clock;
    private final SlackMessageBuilder slackMessageBuilder;

    public SlackService(NotificationStateService notificationStateService,
                        SlackClient slackClient,
                        Clock clock,
                        SlackMessageBuilder slackMessageBuilder) {
        this.notificationStateService = notificationStateService;
        this.slackClient = slackClient;
        this.clock = clock;
        this.slackMessageBuilder = slackMessageBuilder;
    }

    @Transactional
    public void sendNotification(Long notificationId) {
        Instant now = Instant.now(clock);
        Optional<Notification> optionalNotification = notificationStateService.getSendingNotification(notificationId);
        if (optionalNotification.isEmpty()) {
            log.warn("Failed to get SENDING notification: notificationId={}", notificationId);
            return;
        }

        Notification notification = optionalNotification.get();
        String message = slackMessageBuilder.build(notification);

        try {
            slackClient.send(message);
            notificationStateService.markSent(notificationId, now);
        } catch (Exception e) {
            log.error("Failed to send Slack notification. notificationId={}", notificationId, e);
            notificationStateService.markFailed(notificationId);
        }
    }
}
