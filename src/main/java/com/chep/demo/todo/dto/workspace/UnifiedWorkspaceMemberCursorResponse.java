package com.chep.demo.todo.dto.workspace;

import com.chep.demo.todo.domain.workspace.UnifiedWorkspaceMember;

import java.time.Instant;
import java.util.List;

public record UnifiedWorkspaceMemberCursorResponse(List<UnifiedWorkspaceMember> members, boolean hasNext, Integer cursorTypePriority, Instant cursorSortAt, Long cursorRowId) {
}
