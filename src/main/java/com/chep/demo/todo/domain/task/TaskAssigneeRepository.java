package com.chep.demo.todo.domain.task;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskAssigneeRepository {
    @Query("""
            SELECT ta
            FROM TaskAssignee ta
            JOIN FETCH ta.user
            WHERE ta.task.id = :taskId
            """)
    List<TaskAssignee> findAssigneesWithUser(@Param("taskId") Long taskId);
}
