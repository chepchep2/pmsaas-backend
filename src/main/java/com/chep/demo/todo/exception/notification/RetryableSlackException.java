package com.chep.demo.todo.exception.notification;

public class RetryableSlackException extends RuntimeException {
    public RetryableSlackException(String message) {
        super(message);
    }
}
