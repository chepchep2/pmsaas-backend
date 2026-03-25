package com.chep.demo.todo.dto.task;

import jakarta.validation.constraints.NotNull;

public record MoveTaskRequest(
        @NotNull(message = "targetOrderIndex is required")
        Integer targetOrderIndex
) {
}
