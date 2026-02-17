package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.service.RedisKeys;
import com.chep.demo.todo.service.notification.processor.NotificationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueueConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationQueueConsumer.class);
    private static final int IDLE_SLEEP_MS = 200;
    private static final int SEND_INTERVALS_MS = 200;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationProcessor notificationProcessor;

    public NotificationQueueConsumer(RedisTemplate<String, String> redisTemplate,
                                     NotificationProcessor notificationProcessor) {
        this.redisTemplate = redisTemplate;
        this.notificationProcessor = notificationProcessor;
    }

    public void startConsuming() {
        while (true) {
            try {
                String msg = redisTemplate.opsForList().rightPopAndLeftPush(RedisKeys.NOTIFICATION_QUEUE, RedisKeys.NOTIFICATION_PROCESSING);

                if (msg == null) {
                    Thread.sleep(IDLE_SLEEP_MS);
                    continue;
                }

                Long id = Long.parseLong(msg);
                notificationProcessor.process(id);

                redisTemplate.opsForList().remove(RedisKeys.NOTIFICATION_PROCESSING, 1, msg);

                Thread.sleep(SEND_INTERVALS_MS);
            } catch (Exception e) {
                log.error("Error consuming notification queue", e);
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startOnApplicationReady() {
        new Thread(this::startConsuming, "notification-consumer").start();
    }
}
