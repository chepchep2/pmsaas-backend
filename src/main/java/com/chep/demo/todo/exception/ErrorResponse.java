package com.chep.demo.todo.exception;

public record ErrorResponse(String timestamp, int status, String error, String errorCode, String message, String path) { }
