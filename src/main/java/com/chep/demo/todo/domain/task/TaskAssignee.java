package com.chep.demo.todo.domain.task;

import com.chep.demo.todo.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "task_assignees",
uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_task_assignee_task_user",
                columnNames = {"task_id", "user_id"}
        )
    }
)
public class TaskAssignee {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_assignees_id")
    @SequenceGenerator(name = "task_assignees_id", sequenceName = "task_assignee_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TaskAssignee() {}

    private TaskAssignee(Task task, User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (task == null) {
            throw new IllegalArgumentException("todo must not be null");
        }

        this.user = user;
        this.task = task;
        this.createdAt = Instant.now();
    }

    public static class Builder {
        private User user;
        private Task task;

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
            return this;
        }

        public TaskAssignee build() {
            return new TaskAssignee(task, user);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public User getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
