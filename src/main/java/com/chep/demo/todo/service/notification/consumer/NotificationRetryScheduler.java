package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.notification.NotificationRepository;
import com.chep.demo.todo.service.RedisKeys;
import com.chep.demo.todo.service.notification.NotificationStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationRetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);
    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final Clock clock;
    private static final int STUCK_THRESHOLD_SECONDS = 300;
    private static final int SCHEDULER_INTERVAL_MS = 60_000;
    private final NotificationStateService notificationStateService;
    private static final int MAX_ATTEMPTS = 3;

    NotificationRetryScheduler(NotificationRepository notificationRepository, RedisTemplate<String, String> redisTemplate, Clock clock, NotificationStateService notificationStateService) {
        this.notificationRepository = notificationRepository;
        this.redisTemplate = redisTemplate;
        this.clock = clock;
        this.notificationStateService = notificationStateService;
    }
    
    @Scheduled(fixedDelay = SCHEDULER_INTERVAL_MS)
    public void requeueStuckPendingNotifications() {
        Instant threshold = Instant.now(clock).minusSeconds(STUCK_THRESHOLD_SECONDS);
        List<Notification> stuck = notificationRepository.findStuckPendingNotifications(threshold);
        if (stuck.isEmpty()) {
            return;
        }
        log.info("Requeueing {} stuck PENDING notifications", stuck.size());

        List<String> ids = new ArrayList<>();
        for (Notification n : stuck) {
            if (n.getAttemptCount() >= MAX_ATTEMPTS) {
                notificationStateService.markFailedFromPending(n.getId());
            } else {
                ids.add(n.getId().toString());
            }
        }
        if (!ids.isEmpty()) {
            redisTemplate.opsForList().leftPushAll(RedisKeys.RETRY_QUEUE, ids);
        }
    }
}
