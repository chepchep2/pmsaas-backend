package com.chep.demo.todo.infrastructure.persistence.notification;


import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.workspace.NotificationQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {
    private final EntityManager em;

    public NotificationQueryRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Notification> findUserNotifications(Long userId, Long workspaceId, Instant cursorCreatedAt, Long cursorId, int limit) {
        boolean hasWorkspaceId = workspaceId != null;
        boolean hasCursor = cursorCreatedAt != null && cursorId != null;

        StringBuilder jpql = new StringBuilder("""
                SELECT n
                FROM Notification n
                JOIN FETCH n.task t
                WHERE n.recipientType = 'USER'
                AND n.recipientId = :userId
                """);

        if (hasWorkspaceId) {
            jpql.append("AND n.workspaceId = :workspaceId\n");
        }
        if (hasCursor) {
            jpql.append("""
                    AND (n.createdAt < :cursorCreatedAt
                    OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
                    """);
        }
        jpql.append("ORDER BY n.createdAt DESC, n.id DESC");

        TypedQuery<Notification> query = em.createQuery(jpql.toString(), Notification.class);
        query.setParameter("userId", userId);
        if (hasWorkspaceId) query.setParameter("workspaceId", workspaceId);
        if (hasCursor) {
            query.setParameter("cursorCreatedAt", cursorCreatedAt);
            query.setParameter("cursorId", cursorId);
        }
        query.setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public List<Notification> findWorkspaceNotifications(Long workspaceId, Instant cursorCreatedAt, Long cursorId, int limit) {
        boolean hasCursor = cursorCreatedAt != null && cursorId != null;

        StringBuilder jpql = new StringBuilder("""
                SELECT n               
                FROM Notification n
                JOIN FETCH n.task t
                WHERE n.recipientType = 'WORKSPACE'
                AND n.recipientId = :workspaceId
                """);

        if (hasCursor) {
            jpql.append("""
                    AND (n.createdAt < :cursorCreatedAt
                    OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
                    """);
        }
        jpql.append("ORDER BY n.createdAt DESC, n.id DESC");

         TypedQuery<Notification> query = em.createQuery(jpql.toString(), Notification.class);
        query.setParameter("workspaceId", workspaceId);
        if (hasCursor) {
            query.setParameter("cursorCreatedAt", cursorCreatedAt);
            query.setParameter("cursorId", cursorId);
        }
        query.setMaxResults(limit);

        return query.getResultList();
    }
}