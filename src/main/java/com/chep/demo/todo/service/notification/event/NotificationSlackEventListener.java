package com.chep.demo.todo.service.notification.event;

import com.chep.demo.todo.service.notification.producer.NotificationQueueProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class NotificationSlackEventListener {
    private final NotificationQueueProducer notificationQueueProducer;

    public NotificationSlackEventListener(NotificationQueueProducer notificationQueueProducer) {
        this.notificationQueueProducer = notificationQueueProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(WorkspaceNotificationsCreatedEvent event) {
        for (Long id : event.notificationIds()) {
            notificationQueueProducer.push(id);
        }
    }
}
