package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.notification.NotificationRepository;
import com.chep.demo.todo.domain.task.TaskAssignee;
import com.chep.demo.todo.domain.task.TaskAssigneeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SlackService {
    private static final Logger log = LoggerFactory.getLogger(SlackService.class);
    private final NotificationStateService notificationStateService;
    private final SlackClient slackClient;
    private final Clock clock;
    private final SlackMessageBuilder slackMessageBuilder;
    private final TaskAssigneeRepository taskAssigneeRepository;

    public SlackService(NotificationStateService notificationStateService,
                        SlackClient slackClient,
                        Clock clock,
                        SlackMessageBuilder slackMessageBuilder,
                        TaskAssigneeRepository taskAssigneeRepository) {
        this.notificationStateService = notificationStateService;
        this.slackClient = slackClient;
        this.clock = clock;
        this.slackMessageBuilder = slackMessageBuilder;
        this.taskAssigneeRepository = taskAssigneeRepository;
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
        Long taskId = notification.getTask().getId();
        List<TaskAssignee> assignees = taskAssigneeRepository.findAssigneesWithUser(taskId);
        String message = slackMessageBuilder.build(notification, assignees);

        try {
            slackClient.send(message);
            notificationStateService.markSent(notificationId, now);
        } catch (Exception e) {
            log.error("Failed to send Slack notification. notificationId={}", notificationId, e);
            notificationStateService.markFailed(notificationId);
        }
    }
}
