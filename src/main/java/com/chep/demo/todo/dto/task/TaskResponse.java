package com.chep.demo.todo.dto.task;

import java.time.Instant;
import java.util.List;

public record TaskResponse(
        Long id,
        String title,
        String content,
        boolean completed,
        Integer orderIndex,
        Instant dueDate,
        Long projectId,
        List<Long> assigneeIds
) {
}
