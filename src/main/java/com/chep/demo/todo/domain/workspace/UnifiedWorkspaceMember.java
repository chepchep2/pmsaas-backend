package com.chep.demo.todo.domain.workspace;

import com.chep.demo.todo.domain.invitation.Invitation;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(of = {"rowId", "type"})
public class UnifiedWorkspaceMember {
    private Long rowId;
    private String email;
    private String name;
    private WorkspaceMember.Role role;
    private MemberType type;
    private Instant sortAt;
    private Invitation.Status invitationStatus;

    private UnifiedWorkspaceMember(Long rowId, String email, String name,
                                   WorkspaceMember.Role role, MemberType type,
                                   Instant sortAt, Invitation.Status invitationStatus) {
        this.rowId = rowId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.type = type;
        this.sortAt = sortAt;
        this.invitationStatus = invitationStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long rowId;
        private String email;
        private String name;
        private String roleRaw;
        private String typeRaw;
        private Instant sortAt;
        private String statusRaw;

        public Builder rowId(Long rowId) { this.rowId = rowId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder roleRaw(String roleRaw) { this.roleRaw = roleRaw; return this; }
        public Builder typeRaw(String typeRaw) { this.typeRaw = typeRaw; return this; }
        public Builder sortAt(Instant sortAt) { this.sortAt = sortAt; return this; }
        public Builder statusRaw(String statusRaw) { this.statusRaw = statusRaw; return this; }

        public UnifiedWorkspaceMember build() {
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
                try { role = WorkspaceMember.Role.valueOf(roleRaw); }
                catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid role: " + roleRaw, e); }
            }

            MemberType type;
            try { type = MemberType.valueOf(typeRaw); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid member type: " + typeRaw, e); }

            Invitation.Status invitationStatus = null;
            if (statusRaw != null) {
                try { invitationStatus = Invitation.Status.valueOf(statusRaw); }
                catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid invitation status: " + statusRaw, e); }
            }

            return new UnifiedWorkspaceMember(rowId, email, name, role, type, sortAt, invitationStatus);
        }
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
}
