package com.chep.demo.todo.domain.notification;

import com.chep.demo.todo.domain.project.Project;
import com.chep.demo.todo.domain.task.Task;
import com.chep.demo.todo.domain.user.User;
import com.chep.demo.todo.domain.workspace.Workspace;
import com.chep.demo.todo.exception.notification.NotificationStateException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient", columnList = "recipient_type, recipient_id, id"),
                @Index(name = "idx_notifications_status_sending", columnList = "status, sending_started_at")
        }
    )
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_id_gen")
    @SequenceGenerator(name = "notification_id_gen", sequenceName = "notification_id_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 20)
    private RecipientType recipientType;

    @NotNull
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @NotNull
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "sending_started_at")
    private Instant sendingStartedAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @NotNull
    @Column(name = "pending_at")
    private Instant pendingAt;

    protected Notification() {
    }

    private Notification(RecipientType recipientType,
                         Long recipientId,
                         NotificationType type,
                         Task task,
                         User actor,
                         Workspace workspace,
                         Project project,
                         Instant createdAt,
                         Instant pendingAt) {
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.type = type;
        this.task = task;
        this.actor = actor;
        this.workspace = workspace;
        this.project = project;
        this.createdAt = createdAt;
        this.status = NotificationStatus.PENDING;
        this.attemptCount = 0;
        this.sendingStartedAt = null;
        this.pendingAt = pendingAt;
    }

    public static Notification forWorkspace(NotificationType type,
                                            Workspace workspace,
                                            Task task,
                                            User actor,
                                            Project project,
                                            Instant now) {
        return new Notification(
                RecipientType.WORKSPACE,
                workspace.getId(),
                type,
                task,
                actor,
                workspace,
                project,
                now,
                now
        );
    }

    public static Notification forUser(NotificationType type,
                                       Long userId,
                                       Workspace workspace,
                                       Task task,
                                       User actor,
                                       Project project,
                                       Instant now) {
        return new Notification(
                RecipientType.USER,
                userId,
                type,
                task,
                actor,
                workspace,
                project,
                now,
                now
        );
    }

    public boolean isWorkspaceNotification() {
        return recipientType == RecipientType.WORKSPACE;
    }

    public void markSending(Instant now) {
        requirePending();
        this.status = NotificationStatus.SENDING;
        this.sendingStartedAt = now;
        this.attemptCount++;
    }

    public void markSent(Instant now) {
        requireSending();
        this.status = NotificationStatus.SENT;
        this.sentAt = now;
    }

    public void markFailed() {
        requireSending();
        this.status = NotificationStatus.FAILED;
    }

    public void markPending(Instant now) {
        requireSending();
        this.status = NotificationStatus.PENDING;
        this.pendingAt = now;
    }

    public void markFailedFromPending() {
        requirePending();
        this.status = NotificationStatus.FAILED;
    }

    private void requirePending() {
        if (this.status != NotificationStatus.PENDING) {
            throw new NotificationStateException("Only PENDING notifications can be marked as SENDING");
        }
    }

    private void requireSending() {
        if (this.status != NotificationStatus.SENDING) {
            throw new NotificationStateException("Only SENDING notifications can be marked as SENT/FAILED");
        }
    }

    public Long getId() {
        return id;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public Task getTask() {
        return task;
    }

    public User getActor() {
        return actor;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public Project getProject() {
        return project;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getSendingStartedAt() {
        return sendingStartedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPendingAt() {
        return pendingAt;
    }
}
