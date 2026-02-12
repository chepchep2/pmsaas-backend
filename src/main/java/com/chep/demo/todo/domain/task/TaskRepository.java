package com.chep.demo.todo.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserIdOrderByOrderIndexAsc(Long userId);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    Long countByUserId(Long userId);
    List<Task> findByUserIdAndOrderIndexBetween(Long userId, int start, int end);
    List<Task> findByUserIdAndOrderIndexGreaterThan(Long userId, int start);

    default void softDelete(Task task) {
        task.markDeleted();
        save(task);
    }
}
