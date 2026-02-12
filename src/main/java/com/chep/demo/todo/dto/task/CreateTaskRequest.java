package com.chep.demo.todo.dto.task;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CreateTaskRequest(
        @NotEmpty(message = "Title is required")
        String title,
        String content,
        Integer orderIndex,
        Instant dueDate,
        List<Long> assigneeIds,
        Long projectId
) {}