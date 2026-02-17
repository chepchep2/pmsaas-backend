package com.chep.demo.todo.exception.notification;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotificationStateException extends RuntimeException {
    public NotificationStateException(String message) {
        super(message);
    }
}
