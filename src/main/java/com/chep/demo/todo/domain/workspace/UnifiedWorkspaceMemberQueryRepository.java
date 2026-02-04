package com.chep.demo.todo.domain.workspace;

import java.time.Instant;
import java.util.List;

public interface UnifiedWorkspaceMemberQueryRepository {
    List<UnifiedWorkspaceMember> findWithCursor(
            Long workspaceId,
            Integer cursorTypePriority,
            Instant cursorSortAt,
            Long cursorRowId,
            String keyword,
            int limit
    );
}
