package com.chep.demo.todo.domain.task;

import com.chep.demo.todo.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskReorderTest {
    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("tester")
                .email("tester@example.com")
                .password("12345678")
                .build();
    }

    @Test
    void moves_task_upwards_and_shifts_neighbors_down() {
        Task target = buildTask(3);
        Task first = buildTask(1);
        Task second = buildTask(2);

        List<Task> changed = Task.reorder(target, 1, List.of(first, second));

        assertThat(first.getOrderIndex()).isEqualTo(2);
        assertThat(second.getOrderIndex()).isEqualTo(3);
        assertThat(target.getOrderIndex()).isEqualTo(1);
        assertThat(changed).containsExactly(first, second, target);
    }

    @Test
    void moves_task_downwards_and_shifts_neighbors_up() {
        Task target = buildTask(1);
        Task first = buildTask(2);
        Task second = buildTask(3);

        List<Task> changed = Task.reorder(target, 3, List.of(first, second));

        assertThat(first.getOrderIndex()).isEqualTo(1);
        assertThat(second.getOrderIndex()).isEqualTo(2);
        assertThat(target.getOrderIndex()).isEqualTo(3);
        assertThat(changed).containsExactly(first, second, target);
    }

    @Test
    void returns_empty_when_target_index_is_same() {
        Task target = buildTask(2);

        List<Task> changed = Task.reorder(target, 2, List.of());

        assertThat(changed).isEmpty();
        assertThat(target.getOrderIndex()).isEqualTo(2);
    }

    @Test
    void updates_target_even_when_no_neighbors_exist() {
        Task target = buildTask(0);

        List<Task> changed = Task.reorder(target, 3, List.of());

        assertThat(changed).containsExactly(target);
        assertThat(target.getOrderIndex()).isEqualTo(3);
    }

    @Test
    void rejects_negative_target_index() {
        Task target = buildTask(1);

        assertThatThrownBy(() -> Task.reorder(target, -1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetIndex");
    }

    private Task buildTask(int orderIndex) {
        return Task.builder()
                .user(owner)
                .title("task" + orderIndex)
                .content("content")
                .orderIndex(orderIndex)
                .dueDate(Instant.now())
                .build();
    }
}
