package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.service.RedisKeys;
import com.chep.demo.todo.service.notification.processor.NotificationProcessor;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class NotificationQueueConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationQueueConsumer.class);
    private static final int IDLE_SLEEP_MS = 200;
    private static final int SEND_INTERVALS_MS = 200;
    private static final int ERROR_BACKOFF_MS = 1000;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationProcessor notificationProcessor;
    private volatile boolean running = true;

    public NotificationQueueConsumer(RedisTemplate<String, String> redisTemplate,
                                     NotificationProcessor notificationProcessor) {
        this.redisTemplate = redisTemplate;
        this.notificationProcessor = notificationProcessor;
    }

    @PreDestroy
    public void stop() {
        running = false;
    }

    public void startConsuming() {
        while (running) {
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
                try {
                    Thread.sleep(ERROR_BACKOFF_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startOnApplicationReady() {
        new Thread(this::startConsuming, "notification-consumer").start();
    }
}
