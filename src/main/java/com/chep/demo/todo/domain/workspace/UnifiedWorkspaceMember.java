package com.chep.demo.todo.domain.workspace;

import java.time.Instant;

public class UnifiedWorkspaceMember {
    private Long rowId;
    private String email;
    private String name;
    private WorkspaceMember.Role role;
    private MemberType type;
    private Instant sortAt;

    public enum MemberType {
        MEMBER,
        INVITATION
    }

    public UnifiedWorkspaceMember(Long rowId, String email, String name,
                                  String role, String type, Instant sortAt) {
        this.rowId = rowId;
        this.email = email;
        this.name = name;
        this.role = role != null ? WorkspaceMember.Role.valueOf(role) : null;
        this.type = MemberType.valueOf(type);
        this.sortAt = sortAt;
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
