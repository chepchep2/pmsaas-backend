package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.exception.notification.RetryableSlackException;
import com.chep.demo.todo.service.notification.NotificationStateService;
import com.chep.demo.todo.service.notification.processor.NotificationProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class NotificationQueueConsumerTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationProcessor notificationProcessor;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private NotificationStateService notificationStateService;
    @InjectMocks
    private NotificationQueueConsumer consumer;

    @Test
    void startConsuming_whenMessageExists_callsProcessor() {
        // 1. 준비
        ListOperations<String, String> listOps = mock(ListOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPopAndLeftPush(any(), any()))
                .thenReturn("1")
                .thenReturn(null);

        doAnswer(inv -> {
            Field field = NotificationQueueConsumer.class.getDeclaredField("running");
            field.setAccessible(true);
            field.set(consumer, false);
            return null;
        }).when(notificationProcessor).process(1L);
        // 2. 실행
        consumer.startConsuming();
        // 3. 검증
        verify(notificationProcessor).process(1L);
    }

    @Test
    void startConsuming_whenRetryableException_pushesToRetryQueue() {
        // 1. 준비
        ListOperations<String, String> listOps = mock(ListOperations.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(any())).thenReturn(1L);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPopAndLeftPush(any(), any()))
                .thenReturn("1")
                .thenReturn(null);

        doAnswer(inv -> {
            Field field = NotificationQueueConsumer.class.getDeclaredField("running");
            field.setAccessible(true);
            field.set(consumer, false);
            throw new RetryableSlackException("slack error");
        }).when(notificationProcessor).process(1L);
        // 2. 실행
        consumer.startConsuming();
        // 3. 검증
        verify(listOps).leftPush(any(), eq("1"));
        verify(notificationStateService).markPending(1L);
    }

    @Test
    void startConsuming_whenRetryCountExceeded_marsFailed() {
        // 1. 준비
        ListOperations<String, String> listOps = mock(ListOperations.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(any())).thenReturn(4L);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPopAndLeftPush(any() , any()))
                .thenReturn("1")
                .thenReturn(null);

        doAnswer(inv -> {
            Field field = NotificationQueueConsumer.class.getDeclaredField("running");
            field.setAccessible(true);
            field.set(consumer, false);
            throw new RetryableSlackException("slack error");
        }).when(notificationProcessor).process(1L);

        // 2. 실행
        consumer.startConsuming();

        // 3. 검증
        verify(notificationStateService).markFailed(1L);
        verify(redisTemplate).delete(any(String.class));
    }
}
