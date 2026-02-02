package com.chep.demo.todo.domain.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    @Query("""
            SELECT LOWER(m.user.email) FROM WorkspaceMember m
            WHERE
                m.workspace.id = :workspaceId
            AND
                m.status = com.chep.demo.todo.domain.workspace.WorkspaceMember.Status.ACTIVE
            """)
    List<String> findActiveMemberEmails(@Param("workspaceId") Long workspaceId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);
}

// @Query(value = """
//     SELECT wm.* FROM workspace_member wm
//     JOIN user u ON wm.user_id = u.id
//     WHERE wm.workspace_id = :workspaceId
//         AND wm.status = :status
//         AND MATCH(u.name, u.email) AGAINST(:keyword IN BOOLEAN MODE)
//     ORDER BY wm.joined_at DESC
//     LIMIT :limit
//     """, nativeQuery = true)
// List<WorkspaceMember> findWithFullTextSearch(...);