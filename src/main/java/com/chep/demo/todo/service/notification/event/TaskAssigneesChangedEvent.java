package com.chep.demo.todo.service.notification.event;

import java.util.List;

public record TaskAssigneesChangedEvent(Long taskId, Long actorId, List<Long> addedAssigneeIds) {
}
