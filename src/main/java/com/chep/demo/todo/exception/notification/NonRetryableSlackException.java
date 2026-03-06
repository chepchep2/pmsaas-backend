package com.chep.demo.todo.exception.notification;

public class NonRetryableSlackException extends RuntimeException {
    public NonRetryableSlackException(String message) {
        super(message);
    }
}
