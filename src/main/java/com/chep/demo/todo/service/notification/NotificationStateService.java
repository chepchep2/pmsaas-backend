package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.domain.notification.NotificationStatus;
import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.notification.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Transactional
@Service
public class NotificationStateService {
    private final NotificationRepository notificationRepository;
    private final Clock clock;
    private static final Logger log = LoggerFactory.getLogger(NotificationStateService.class);

    public NotificationStateService(NotificationRepository notificationRepository, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    public boolean tryMarkSending(Long notificationId) {
        Instant now = Instant.now(clock);
        int updateNotification = notificationRepository.tryMarkSending(notificationId, now);
        return updateNotification == 1;
    }

    @Transactional(readOnly = true)
    public Optional<Notification> getSendingNotification(Long notificationId) {
        return notificationRepository.findForSlackSend(notificationId)
                .filter(n -> n.getStatus() == NotificationStatus.SENDING);
    }

    public void markSent(Long notificationId, Instant sentAt) {
        log.info("markSent called: {}", notificationId);
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isEmpty()) {
            log.warn("Notification not found for markSent: {}", notificationId);
            return;
        }
        Notification noti = optionalNotification.get();
        noti.markSent(sentAt);
    }

    public void markFailed(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isEmpty()) {
            log.warn("Notification not found for markFailed: {}", notificationId);
            return;
        }
        Notification noti = optionalNotification.get();
        noti.markFailed();
    }

    public void markPending(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isEmpty()) {
            log.warn("Notification not found for markPending: {}", notificationId);
            return;
        }
        Notification noti = optionalNotification.get();
        noti.markPending();
    }
}
