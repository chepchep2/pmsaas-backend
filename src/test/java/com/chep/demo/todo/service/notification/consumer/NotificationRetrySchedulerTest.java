package com.chep.demo.todo.service.notification.consumer;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.notification.NotificationRepository;
import com.chep.demo.todo.service.RedisKeys;
import com.chep.demo.todo.service.notification.NotificationStateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationRetrySchedulerTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationStateService notificationStateService;
    @Mock
    private Clock clock;
    @InjectMocks
    private NotificationRetryScheduler notificationRetryScheduler;

    @Test
    void requeueStuckPendingNotifications_whenStuckExists_pushesToRetryQueue() {
        // 준비
        Notification notification = mock(Notification.class);
        when(notification.getId()).thenReturn(1L);
        when(notification.getAttemptCount()).thenReturn(1);

        ListOperations<String, String> listOps = mock(ListOperations.class);
        when(clock.instant()).thenReturn(Instant.parse("2026-03-13T00:00:00Z"));
        when(notificationRepository.findStuckPendingNotifications(any()))
                .thenReturn(List.of(notification));
        when(redisTemplate.opsForList()).thenReturn(listOps);
        // 실행
        notificationRetryScheduler.requeueStuckPendingNotifications();
        // 검증
        verify(listOps).leftPushAll(eq(RedisKeys.RETRY_QUEUE), anyList());
    }

    @Test
    void requeueStuckPendingNotifications_whenNoStuck_doesNothing() {
        // 준비
        when(clock.instant()).thenReturn(Instant.parse("2026-03-13T00:00:00Z"));
        when(notificationRepository.findStuckPendingNotifications(any())).thenReturn(List.of());
        // 실행
        notificationRetryScheduler.requeueStuckPendingNotifications();
        // 검증
        verify(redisTemplate, never()).opsForList();
    }

    @Test
    void requeueStuckPendingNotifications_whenAttemptCountExceeded_marksFailed() {
        // 준비
        Notification notification = mock(Notification.class);
        when(notification.getId()).thenReturn(1L);
        when(notification.getAttemptCount()).thenReturn(3);

        when(clock.instant()).thenReturn(Instant.parse("2026-03-13T00:00:00Z"));
        when(notificationRepository.findStuckPendingNotifications(any()))
                .thenReturn(List.of(notification));

        // 실행
        notificationRetryScheduler.requeueStuckPendingNotifications();

        // 검증
        verify(notificationStateService).markFailedFromPending(1L);
    }

    @Test
    void requeueStuckPendingNotifications_whenNotStuckYet_doesNothing() {
        //  준비
        when(clock.instant()).thenReturn(Instant.parse("2026-03-13T00:00:00Z"));
        when(notificationRepository.findStuckPendingNotifications(any()))
                .thenReturn(List.of());

        // 실행
        notificationRetryScheduler.requeueStuckPendingNotifications();

        // 검증
        verify(redisTemplate, never()).opsForList();
    }
}
