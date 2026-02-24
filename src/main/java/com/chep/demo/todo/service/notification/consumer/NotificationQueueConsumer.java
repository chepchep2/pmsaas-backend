package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.exception.notification.NonRetryableSlackException;
import com.chep.demo.todo.exception.notification.RetryableSlackException;
import com.chep.demo.todo.service.RedisKeys;
import com.chep.demo.todo.service.notification.NotificationStateService;
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
import java.util.concurrent.TimeUnit;

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
    private final CountDownLatch latch = new CountDownLatch(2);
    private final NotificationStateService notificationStateService;
    private static final int RETRY_INTERVALS_MS = 3000;

    public NotificationQueueConsumer(RedisTemplate<String, String> redisTemplate,
                                     NotificationProcessor notificationProcessor,
                                     @Qualifier("notificationExecutor") TaskExecutor taskExecutor,
                                     NotificationStateService notificationStateService) {
        this.redisTemplate = redisTemplate;
        this.notificationProcessor = notificationProcessor;
        this.taskExecutor = taskExecutor;
        this.notificationStateService = notificationStateService;
    }

    public void startConsuming() {
        try {
            while (running) {
                String msg = null;
                Long id = null;
                try {
                    msg = redisTemplate.opsForList().rightPopAndLeftPush(RedisKeys.NOTIFICATION_QUEUE, RedisKeys.NOTIFICATION_PROCESSING);

                    if (msg == null) {
                        Thread.sleep(IDLE_SLEEP_MS);
                        continue;
                    }

                    id = Long.parseLong(msg);
                    notificationProcessor.process(id);

                    Thread.sleep(SEND_INTERVALS_MS);
                } catch (RetryableSlackException e) {
                    handleRetryableFailure(id, msg);
                } catch (NonRetryableSlackException e) {
                    log.error("Non-retryable slack error. id={}", id, e);
                    notificationStateService.markFailed(id);
                } catch (Exception e) {
                    log.error("Error consuming notification queue", e);
                    try {
                        Thread.sleep(ERROR_BACKOFF_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    if (msg != null) {
                        redisTemplate.opsForList().remove(RedisKeys.NOTIFICATION_PROCESSING, 1, msg);
                    }
                }
            }
        } finally {
            latch.countDown();
        }
    }

    public void startRetryConsuming() {
        try {
            while (running) {
                String msg = null;
                Long id = null;
                try {
                    msg = redisTemplate.opsForList().rightPopAndLeftPush(RedisKeys.RETRY_QUEUE, RedisKeys.NOTIFICATION_PROCESSING);

                    if (msg == null) {
                        Thread.sleep(IDLE_SLEEP_MS);
                        continue;
                    }

                    id = Long.parseLong(msg);
                    notificationProcessor.process(id);

                    Thread.sleep(RETRY_INTERVALS_MS);
                } catch (RetryableSlackException e) {
                    handleRetryableFailure(id, msg);
                } catch (NonRetryableSlackException e) {
                    log.error("Non-retryable slack error. id={}", id, e);
                    notificationStateService.markFailed(id);
                } catch (Exception e) {
                    log.error("Error consuming notification queue", e);
                    try {
                        Thread.sleep(ERROR_BACKOFF_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    if (msg != null) {
                        redisTemplate.opsForList().remove(RedisKeys.NOTIFICATION_PROCESSING, 1, msg);
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
        taskExecutor.execute(this::startRetryConsuming);
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

    private void handleRetryableFailure(Long id, String msg) {
        String retryKey = RedisKeys.RETRY_COUNT_PREFIX + id;
        Long retryCount = redisTemplate.opsForValue().increment(retryKey);
        if (retryCount == 1) {
            redisTemplate.expire(retryKey, 1, TimeUnit.DAYS);
        }
        if (retryCount <= 3) {
            log.warn("Retryable slack error. id={} retryCount={}", id, retryCount);
            redisTemplate.opsForList().leftPush(RedisKeys.RETRY_QUEUE, msg);
        } else {
            log.error("Max retry exceeded. id={}", id);
            notificationStateService.markFailed(id);
            redisTemplate.delete(retryKey);
        }
    }
}
