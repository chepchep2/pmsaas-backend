package com.chep.demo.todo.exception;

import com.chep.demo.todo.common.constant.ErrorCode;
import com.chep.demo.todo.exception.auth.AuthenticationException;
import com.chep.demo.todo.exception.auth.UserNotFoundException;
import com.chep.demo.todo.exception.invitation.InvitationValidationException;
import com.chep.demo.todo.exception.invitation.InviteCodeExpiredException;
import com.chep.demo.todo.exception.invitation.InviteCodeNotFoundException;
import com.chep.demo.todo.exception.invitation.InviteCodeWorkspaceMismatchException;
import com.chep.demo.todo.exception.project.ProjectNotFoundException;
import com.chep.demo.todo.exception.task.TaskNotFoundException;
import com.chep.demo.todo.exception.workspace.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse createErrorResponse(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        return new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.error("Invalid argument. errorCode={}, path={}, message={}", ErrorCode.INVALID_ARGUMENT, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, e.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Validation failed. errorCode={}, path={}, message={}",
                ErrorCode.VALIDATION_FAILED, request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message, request));
    }

    @ExceptionHandler(InviteCodeWorkspaceMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInviteCodeMismatch(InviteCodeWorkspaceMismatchException e, HttpServletRequest request) {
        log.error("Invite code workspace mismatch. errorCode={}, path={}, message={}", ErrorCode.INVITATION_WORKSPACE_MISMATCH, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVITATION_WORKSPACE_MISMATCH, e.getMessage(), request));
    }

    @ExceptionHandler(InvitationValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvitationValidation(
            InvitationValidationException e,
            HttpServletRequest request
    ) {
        log.error("Invitation validation failed. errorCode={}, path={}, message={}", ErrorCode.INVITATION_VALIDATION_FAILED, request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVITATION_VALIDATION_FAILED, e.getMessage(), request));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("Authentication failed. errorCode={}, path={}, message={}", ErrorCode.AUTHENTICATION_FAILED, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_FAILED, e.getMessage(), request));
    }

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerWorkspaceNotFoundException(WorkspaceNotFoundException e, HttpServletRequest request) {
        log.error("Workspace not found. errorCode={}, path={}, message={}",
                ErrorCode.WORKSPACE_NOT_FOUND, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.WORKSPACE_NOT_FOUND, e.getMessage(), request));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerProjectNotFoundException(ProjectNotFoundException e, HttpServletRequest request) {
        log.error("Project not found. errorCode={}, path={}, message={}",
                ErrorCode.PROJECT_NOT_FOUND, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.PROJECT_NOT_FOUND, e.getMessage(), request));
    }

    @ExceptionHandler(WorkspaceAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceAccessDeniedException(WorkspaceAccessDeniedException e, HttpServletRequest request) {
        log.error("Workspace access denied. errorCode={}, path={}, message={}",
                ErrorCode.WORKSPACE_ACCESS_DENIED, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse(HttpStatus.FORBIDDEN, ErrorCode.WORKSPACE_ACCESS_DENIED, e.getMessage(), request));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException e, HttpServletRequest request) {
        log.error("Task not found. errorCode={}, path={}, message={}",
                ErrorCode.TASK_NOT_FOUND, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.TASK_NOT_FOUND, e.getMessage(), request));
    }

    @ExceptionHandler(WorkspaceOwnerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceOwnerNotFoundException(WorkspaceOwnerNotFoundException e, HttpServletRequest request) {
        log.error("Workspace owner not found. errorCode={}, path={}, message={}",
                ErrorCode.WORKSPACE_OWNER_NOT_FOUND, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.WORKSPACE_OWNER_NOT_FOUND, e.getMessage(), request));
    }

    @ExceptionHandler(WorkspaceMemberOperationException.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceMemberOperationException(WorkspaceMemberOperationException e, HttpServletRequest request) {
        log.error("Workspace member operation failed. errorCode={}, path={}, message={}",
                ErrorCode.WORKSPACE_MEMBER_OPERATION_FAILED, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.WORKSPACE_MEMBER_OPERATION_FAILED, e.getMessage(), request));
    }

    @ExceptionHandler(WorkspaceMemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkspaceMemberNotFoundException(WorkspaceMemberNotFoundException e, HttpServletRequest request) {
        log.error("Workspace member not found. errorCode={}, path={}, message={}",
                ErrorCode.WORKSPACE_MEMBER_NOT_FOUND, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.WORKSPACE_MEMBER_NOT_FOUND, e.getMessage(), request));
    }

    @ExceptionHandler(WorkspacePolicyViolationException.class)
    public ResponseEntity<ErrorResponse> handleWorkspacePolicyViolationException(WorkspacePolicyViolationException e, HttpServletRequest request) {
        log.error("Workspace policy violation. errorCode={}, path={}, message={}",
                ErrorCode.WORKSPACE_POLICY_VIOLATION, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.WORKSPACE_POLICY_VIOLATION, e.getMessage(), request));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        log.error("User not found. errorCode={}, path={}, message={}",
                ErrorCode.USER_NOT_FOUND, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, e.getMessage(), request));
    }

    @ExceptionHandler(InviteCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInviteCodeNotFoundException(InviteCodeNotFoundException e, HttpServletRequest request) {
        log.error("Invite code not found. errorCode={}, path={}, message={}",
                ErrorCode.INVITATION_CODE_NOT_FOUND, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.INVITATION_CODE_NOT_FOUND, e.getMessage(), request));
    }

    @ExceptionHandler(InviteCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleInviteCodeExpiredException(InviteCodeExpiredException e, HttpServletRequest request) {
        log.error("Invite code expired. errorCode={}, path={}, message={}",
                ErrorCode.INVITATION_EXPIRED, request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.GONE)
                .body(createErrorResponse(HttpStatus.GONE, ErrorCode.INVITATION_EXPIRED, e.getMessage(), request));
    }
}
