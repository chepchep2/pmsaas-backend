package com.chep.demo.todo.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("""
            SELECT DISTINCT t FROM Task t
            JOIN FETCH t.project p
            LEFT JOIN FETCH t.assignees a
            LEFT JOIN FETCH a.user u
            WHERE t.project.workspace.id = :workspaceId  
            ORDER BY t.project.id, t.orderIndex
            """)
    List<Task> findAllByWorkspaceId(@Param("workspaceId") Long workspaceId);

    default void softDelete(Task task) {
        task.markDeleted();
        save(task);
    }

    @Query("SELECT t.project.workspace.id FROM Task t WHERE t.id = :taskId")
    Optional<Long> findWorkspaceIdByTaskId(@Param("taskId") Long taskId);
}
