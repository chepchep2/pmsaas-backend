package com.chep.demo.todo.exception.cursor;

public class InvalidCursorTokenException extends RuntimeException {
    public InvalidCursorTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
