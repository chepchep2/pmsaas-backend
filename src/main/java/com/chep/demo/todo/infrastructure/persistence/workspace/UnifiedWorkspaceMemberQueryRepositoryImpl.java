package com.chep.demo.todo.infrastructure.persistence.workspace;

import com.chep.demo.todo.domain.workspace.UnifiedWorkspaceMember;
import com.chep.demo.todo.domain.workspace.UnifiedWorkspaceMemberQueryRepository;
import com.chep.demo.todo.domain.workspace.MemberType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class UnifiedWorkspaceMemberQueryRepositoryImpl implements UnifiedWorkspaceMemberQueryRepository {
    private final EntityManager em;

    public UnifiedWorkspaceMemberQueryRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<UnifiedWorkspaceMember> findWithCursor(Long workspaceId,
                                                       Integer cursorTypePriority,
                                                       Instant cursorSortAt,
                                                       Long cursorRowId,
                                                       String keyword,
                                                       int limit
    ) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCursor = cursorTypePriority != null && cursorSortAt != null && cursorRowId != null;

        String sql = buildUnifiedMembersSql(hasKeyword, hasCursor);

        Query query = createQueryWithParams(
                sql,
                workspaceId,
                keyword,
                cursorTypePriority,
                cursorSortAt,
                cursorRowId,
                limit,
                hasKeyword,
                hasCursor
        );

        List<Object[]> results = query.getResultList();

        return mapResults(results);
    }

    private String buildUnifiedMembersSql(boolean hasKeyword, boolean hasCursor) {
        StringBuilder sql = new StringBuilder();

        sql.append("""
                SELECT row_id,
                       email,
                       name,
                       role,
                       type,
                       type_priority,
                       sort_at
                    FROM (
                    SELECT 
                        wm.id AS row_id,
                        u.email AS email,
                        u.name AS name,
                        wm.role AS role,
                        'MEMBER' AS type,
                        :memberTypePriority AS type_priority,
                        wm.joined_at AS sort_at
                    FROM workspace_members wm
                    LEFT JOIN users u ON wm.user_id = u.id
                    WHERE wm.workspace_id = :workspaceId
                        AND wm.status = 'ACTIVE'                    
                """);
        if (hasKeyword) {
            sql.append("""
                    AND (
                        LOWER(u.email) LIKE :keyword
                        OR LOWER(u.name) LIKE :keyword
                    )
                    """);
        }

        sql.append("""
                UNION ALL
                
                SELECT
                    i.id AS row_id,
                    i.sent_email AS email,
                    NULL AS name,
                    NULL AS role,
                    'INVITATION' AS type,
                    :invitationTypePriority AS type_priority,
                    i.created_at AS sort_at
                FROM invitations i
                LEFT JOIN invitation_codes ic ON i.invite_code_id = ic.id
                WHERE ic.workspace_id = :workspaceId
                    AND i.status = 'SENT'
                """);

        if (hasKeyword) {
            sql.append("""
                    AND LOWER(i.sent_email) LIKE :keyword
                    """);
        }

        sql.append("""
                ) unified
                """);

        if (hasCursor) {
            sql.append("""
                    WHERE (
                        type_priority > :cursorTypePriority
                        OR (
                            type_priority = :cursorTypePriority
                            AND sort_at < :cursorSortAt
                        )
                        OR (
                            type_priority = :cursorTypePriority
                            AND sort_at = :cursorSortAt
                            AND row_id < :cursorRowId
                        )
                    )
                    """);
        }

        sql.append("""
                ORDER BY
                    type_priority ASC,
                    sort_at DESC,
                    row_id DESC
                """);

        return sql.toString();
    }

    private Query createQueryWithParams(String sql,
                                        Long workspaceId,
                                        String keyword,
                                        Integer cursorTypePriority,
                                        Instant cursorSortAt, Long cursorRowId,
                                        int limit,
                                        boolean hasKeyword,
                                        boolean hasCursor
    ) {
        Query query = em.createNativeQuery(sql);
        query.setParameter("memberTypePriority", MemberType.MEMBER.getPriority());
        query.setParameter("invitationTypePriority", MemberType.INVITATION.getPriority());
        query.setParameter("workspaceId", workspaceId);
        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (hasCursor) {
            query.setParameter("cursorTypePriority", cursorTypePriority);
            query.setParameter("cursorSortAt", cursorSortAt);
            query.setParameter("cursorRowId", cursorRowId);
        }
        query.setMaxResults(limit);

        return query;
    }

    private List<UnifiedWorkspaceMember> mapResults(List<Object[]> results) {
        return results.stream()
                .map(this::mapToUnifiedMember)
                .toList();
    }

    private UnifiedWorkspaceMember mapToUnifiedMember(Object[] row) {
        return new UnifiedWorkspaceMember(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                row[3] != null ? row[3].toString() : null,
                row[4] != null ? row[4].toString() : null,
                toInstant(row[6])
        );
    }

    private Instant toInstant(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("sort_at cannot be null - data integrity issue");
        }

        if (obj instanceof Instant) {
            return (Instant) obj;
        } else if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toInstant();
        } else if (obj instanceof OffsetDateTime) {
            return ((OffsetDateTime) obj).toInstant();
        } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).atZone(ZoneId.systemDefault())
                    .toInstant();
        } else {
            throw new IllegalArgumentException(
                    "Unexpected date type for sort_at: " + obj.getClass().getName() + ", value: " + obj
            );
        }
    }
}
