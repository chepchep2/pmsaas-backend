package com.chep.demo.todo.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET
                n.status = com.chep.demo.todo.domain.notification.NotificationStatus.SENDING,           
                n.sendingStartedAt = :now,
                n.attemptCount = n.attemptCount + 1
            WHERE
                n.id = :notificationId
            AND
                n.status = com.chep.demo.todo.domain.notification.NotificationStatus.PENDING
            """)
    int tryMarkSending(@Param("notificationId") Long notificationId,
                       @Param("now")Instant now);

    @Query("""
            SELECT n
            FROM Notification n
            JOIN FETCH n.task t                        
            JOIN FETCH n.actor
            JOIN FETCH n.workspace
            JOIN FETCH n.project
            WHERE n.id = :notificationId
            """)
    Optional<Notification> findForSlackSend(@Param("notificationId") Long notificationId);

    @Query("""
            SELECT n 
            FROM Notification n
            WHERE n.status = com.chep.demo.todo.domain.notification.NotificationStatus.PENDING AND n.pendingAt < :before
            """)
    List<Notification> findStuckPendingNotifications(@Param("before") Instant before);
}
