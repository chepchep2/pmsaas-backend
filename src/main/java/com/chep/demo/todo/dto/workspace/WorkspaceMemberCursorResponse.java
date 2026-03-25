package com.chep.demo.todo.dto.workspace;

import java.util.List;

public record WorkspaceMemberCursorResponse(
        List<WorkspaceMemberResponse> members,
        boolean hasNext,
        String nextCursor
) { }
