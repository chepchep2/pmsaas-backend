package com.chep.demo.todo.exception.invitation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InviteCodeWorkspaceMismatchException extends RuntimeException {
    public InviteCodeWorkspaceMismatchException(String message) {
        super(message);
    }
}
