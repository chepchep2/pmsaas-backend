package com.chep.demo.todo.dto.task;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskRequest(
        @NotEmpty(message = "Title is required")
        String title,
        String content
) {
}
