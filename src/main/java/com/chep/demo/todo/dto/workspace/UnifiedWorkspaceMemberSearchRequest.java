package com.chep.demo.todo.dto.workspace;

import java.time.Instant;

public record UnifiedWorkspaceMemberSearchRequest(
        Long workspaceId,
        Integer cursorTypePriority,
        Instant cursorSortAt,
        Long cursorRowId,
        String keyword,
        int limit
) {
    public UnifiedWorkspaceMemberSearchRequest {
        validateCursor(cursorTypePriority, cursorSortAt, cursorRowId);
    }

    private static void validateCursor(Integer tp, Instant sa, Long ri) {
        boolean any = tp != null || sa != null || ri != null;
        boolean all = tp != null && sa != null && ri != null;
        if (any && !all) {
            throw new IllegalArgumentException("cursor parameters must be all present or all absent");
        }
    }

    public boolean hasCursor() {
        return cursorTypePriority != null;
    }
}
