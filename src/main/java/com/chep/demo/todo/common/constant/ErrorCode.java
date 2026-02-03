package com.chep.demo.todo.common.constant;

public class ErrorCode {
    // Common
    public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";

    // Authentication
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";

    // Invitation
    public static final String INVITATION_WORKSPACE_MISMATCH = "INVITE_WORKSPACE_MISMATCH";
    public static final String INVITATION_EXPIRED = "INVITE_EXPIRED";
    public static final String INVITATION_VALIDATION_FAILED = "INVITATION_VALIDATION_FAILED";
    public static final String INVITATION_CODE_NOT_FOUND = "INVITE_CODE_NOT_FOUND";

    // Workspace
    public static final String WORKSPACE_NOT_FOUND = "WORKSPACE_NOT_FOUND";
    public static final String WORKSPACE_ACCESS_DENIED = "WORKSPACE_ACCESS_DENIED";
    public static final String WORKSPACE_OWNER_NOT_FOUND = "WORKSPACE_OWNER_NOT_FOUND";
    public static final String WORKSPACE_MEMBER_NOT_FOUND = "WORKSPACE_MEMBER_NOT_FOUND";
    public static final String WORKSPACE_MEMBER_OPERATION_FAILED = "WORKSPACE_MEMBER_OPERATION_FAILED";
    public static final String WORKSPACE_POLICY_VIOLATION = "WORKSPACE_POLICY_VIOLATION";

    // Project
    public static final String PROJECT_NOT_FOUND = "PROJECT_NOT_FOUND";

    // Todo
    public static final String TODO_NOT_FOUND = "TODO_NOT_FOUND";

    // User
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
}
