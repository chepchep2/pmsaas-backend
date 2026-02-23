package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.service.RedisKeys;
import com.chep.demo.todo.service.notification.processor.NotificationProcessor;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
public class NotificationQueueConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationQueueConsumer.class);
    private static final int IDLE_SLEEP_MS = 200;
    private static final int SEND_INTERVALS_MS = 200;
    private static final int ERROR_BACKOFF_MS = 1000;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationProcessor notificationProcessor;
    private volatile boolean running = true;
    private final TaskExecutor taskExecutor;
    private final CountDownLatch latch = new CountDownLatch(1);

    public NotificationQueueConsumer(RedisTemplate<String, String> redisTemplate,
                                     NotificationProcessor notificationProcessor,
                                     @Qualifier("notificationExecutor") TaskExecutor taskExecutor) {
        this.redisTemplate = redisTemplate;
        this.notificationProcessor = notificationProcessor;
        this.taskExecutor = taskExecutor;
    }

    public void startConsuming() {
        try {
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
        } finally {
            latch.countDown();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startOnApplicationReady() {
        recoverProcessingQueue();
        taskExecutor.execute(this::startConsuming);
    }

    @PreDestroy
    public void stop(){
        running = false;
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void recoverProcessingQueue() {
        Long size = redisTemplate.opsForList().size(RedisKeys.NOTIFICATION_PROCESSING);
        long remainingQueue = size != null ? size : 0L;
        if (remainingQueue > 0) {
            log.info("Recovering {} messages from processing queue", remainingQueue);
        }
        while (remainingQueue != 0) {
            redisTemplate.opsForList().rightPopAndLeftPush(RedisKeys.NOTIFICATION_PROCESSING, RedisKeys.NOTIFICATION_QUEUE);
            remainingQueue--;
        }
    }
}
