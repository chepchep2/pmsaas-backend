package com.chep.demo.todo.domain.workspace;

import com.chep.demo.todo.domain.invitation.Invitation;

import java.time.Instant;
import java.util.Objects;

public class UnifiedWorkspaceMember {
    private Long rowId;
    private String email;
    private String name;
    private WorkspaceMember.Role role;
    private MemberType type;
    private Instant sortAt;
    private Invitation.Status invitationStatus;

    public UnifiedWorkspaceMember(Long rowId, String email, String name,
                                  WorkspaceMember.Role role, MemberType type, Instant sortAt, Invitation.Status invitationStatus) {
        this.rowId = rowId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.type = type;
        this.sortAt = sortAt;
        this.invitationStatus = invitationStatus;
    }

    public static UnifiedWorkspaceMember fromRawStrings(
            Long rowId,
            String email,
            String name,
            String roleRaw,
            String typeRaw,
            Instant sortAt,
            String statusRaw
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

        Invitation.Status invitationStatus = null;
        if (statusRaw != null) {
            try {
                invitationStatus = Invitation.Status.valueOf(statusRaw);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid invitation status: " + statusRaw, e);
            }
        }

        return new UnifiedWorkspaceMember(rowId, email, name, role, type, sortAt, invitationStatus);
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

    public Invitation.Status getInvitationStatus() {
        return invitationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UnifiedWorkspaceMember that = (UnifiedWorkspaceMember) o;
        return Objects.equals(rowId, that.rowId) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowId, type);
    }
}
