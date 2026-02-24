package com.chep.demo.todo.service;

public class RedisKeys {
    public static final String INVITATION_QUEUE = "invitation:queue";
    public static final String NOTIFICATION_QUEUE = "notification:queue";
    public static final String NOTIFICATION_PROCESSING = "notification:processing";
    public static final String RETRY_QUEUE = "notification:retry:queue";
    public static final String RETRY_COUNT_PREFIX = "notification:retry:";
}
