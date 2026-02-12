package com.chep.demo.todo.controller.task;

import com.chep.demo.todo.domain.task.Task;
import com.chep.demo.todo.dto.task.*;
import com.chep.demo.todo.service.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Task", description = "Task 관리 API")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @Operation(
            summary = "Task 목록 조회",
            description = "현재 로그인한 사용자의 Task 목록을 orderIndex 오름차순으로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    ResponseEntity<List<TaskResponse>> getTasks() {
        Long userId = currentUserId();

        List<TaskResponse> responses = taskService.getTasks(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Task 생성",
            description = "새로운 Task를 생성합니다. orderIndex가 null이면 자동으로 마지막 순서에 배치됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 또는 잘못된 assigneeId 포함")
    })
    @PostMapping
    ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Long userId = currentUserId();

        Task created = taskService.createTask(userId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(created));
    }

    @Operation(
            summary = "Task 수정",
            description = "Task의 제목(title)과 내용(content)을 수정합니다. 순서(orderIndex)는 이 API에서 수정하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 권한이 없는 Task ID")
    })
    @PutMapping("/{id}")
    ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        Long userId = currentUserId();

        Task updated = taskService.updateTask(userId, id, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Task 삭제",
            description = "Task를 soft delete 방식으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 권한이 없는 Task ID")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        Long userId = currentUserId();

        taskService.deleteTask(userId, id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Task 완료 토글",
            description = "Task의 completed 상태를 true/false로 토글합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 권한이 없는 Task ID")
    })
    @PatchMapping("/{id}/toggle")
    ResponseEntity<Void> toggleTaskComplete(@PathVariable Long id) {
        Long userId = currentUserId();

        taskService.toggleTaskComplete(userId, id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Task 순서 이동",
            description = "Task의 orderIndex를 재배치합니다. 중간에 있는 Task들의 orderIndex는 자동 조정됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "순서 변경 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 권한이 없는 Task ID")
    })
    @PatchMapping("/{id}/move")
    ResponseEntity<Void> moveTask(
            @PathVariable Long id,
            @Valid @RequestBody MoveTaskRequest request
    ) {
        Long userId = currentUserId();

        taskService.move(userId, id, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "담당자 목록 변경",
            description = "Task의 assigneeIds 전체를 새로운 목록으로 교체합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 assigneeId 포함"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 권한이 없는 Task ID")
    })
    @PatchMapping("/{id}/assignees")
    ResponseEntity<TaskResponse> updateAssignees(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssigneesRequest request
    ) {
        Long userId = currentUserId();
        Task updated = taskService.updateAssignees(userId, id, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Task 마감일 변경",
            description = "Task의 dueDate 값을 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 권한이 없는 Task ID")
    })
    @PatchMapping("/{id}/due-date")
    ResponseEntity<TaskResponse> updateDueDate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDueDateRequest request
    ) {
        Long userId = currentUserId();
        Task updated = taskService.updateDueDate(userId, id, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getContent(),
                task.isCompleted(),
                task.getOrderIndex(),
                task.getDueDate(),
                task.getAssignees().stream()
                        .map(assignee -> assignee.getUser().getId())
                        .toList()
        );
    }
}
