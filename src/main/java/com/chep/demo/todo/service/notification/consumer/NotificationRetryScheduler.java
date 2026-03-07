package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.notification.NotificationRepository;
import com.chep.demo.todo.service.RedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class NotificationRetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);
    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final Clock clock;

    NotificationRetryScheduler(NotificationRepository notificationRepository, RedisTemplate redisTemplate, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.redisTemplate = redisTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 60_000)
    public void requeueStuckPendingNotifications() {
        Instant threshold = Instant.now(clock).minusSeconds(300);
        List<Notification> stuck = notificationRepository.findStuckPendingNotifications(threshold);
        if (stuck.isEmpty()) {
            return;
        }
        log.info("Requeueing {} stuck PENDING notifications", stuck.size());

        for (Notification n : stuck) {
            redisTemplate.opsForList().leftPush(RedisKeys.RETRY_QUEUE, n.getId().toString());
        }
    }
}
