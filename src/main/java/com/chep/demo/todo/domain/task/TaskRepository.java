package com.chep.demo.todo.domain.task;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserIdOrderByOrderIndexAsc(Long userId);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    Long countByProjectId(Long projectId);
    List<Task> findByUserIdAndOrderIndexBetween(Long userId, int start, int end);
    List<Task> findByUserIdAndOrderIndexGreaterThan(Long userId, int start);
    List<Task> findByProjectIdAndOrderIndexBetween(Long projectId, int start, int end);
    List<Task> findAllByProjectIdOrderByOrderIndexAsc(Long projectId);
    List<Task> findByProjectIdAndOrderIndexGreaterThan(Long projectId, int start);
    @Query("SELECT t FROM Task t " +
            "WHERE t.project.workspace.id = :workspaceId " +
            "ORDER BY t.project.id, t.orderIndex")
    List<Task> findAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
    @Query("""
            SELECT DISTINCT t
            FROM Task t
            JOIN FETCH t.project p
            JOIN FETCH p.workspace w
            JOIN FETCH t.user u
            LEFT JOIN FETCH t.assignees a
            LEFT JOIN FETCH a.user au
            WHERE t.id = :taskId
            """)
    Optional<Task> findByIdWithDetails(Long taskId);

    default void softDelete(Task task) {
        task.markDeleted();
        save(task);
    }
}
