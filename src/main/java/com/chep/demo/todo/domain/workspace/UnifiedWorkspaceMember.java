package com.chep.demo.todo.domain.workspace;

import java.time.Instant;

public class UnifiedWorkspaceMember {
    private Long rowId;
    private String email;
    private String name;
    private WorkspaceMember.Role role;
    private MemberType type;
    private Instant sortAt;

    public UnifiedWorkspaceMember(Long rowId, String email, String name,
                                  WorkspaceMember.Role role, MemberType type, Instant sortAt) {
        this.rowId = rowId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.type = type;
        this.sortAt = sortAt;
    }

    public static UnifiedWorkspaceMember fromRawStrings(
            Long rowId,
            String email,
            String name,
            String roleRaw,
            String typeRaw,
            Instant sortAt
    ) {
        if (rowId == null) {
            throw new IllegalArgumentException("rowId must not be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("email must not be null");
        }
        if (typeRaw == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (sortAt == null) {
            throw new IllegalArgumentException("sortAt must not be null");
        }

        WorkspaceMember.Role role = null;
        if (roleRaw != null) {
            try {
                role = WorkspaceMember.Role.valueOf(roleRaw);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + roleRaw, e);
            }
        }

        final MemberType type;
        try {
            type = MemberType.valueOf(typeRaw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid member type: " + typeRaw, e);
        }

        return new UnifiedWorkspaceMember(rowId, email, name, role, type, sortAt);
    }


    public Long getRowId() {
        return rowId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public WorkspaceMember.Role getRole() {
        return role;
    }

    public MemberType getType() {
        return type;
    }

    public Instant getSortAt() {
        return sortAt;
    }
}
