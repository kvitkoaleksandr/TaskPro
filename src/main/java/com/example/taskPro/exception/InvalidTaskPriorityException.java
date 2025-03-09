package com.example.taskPro.exception;

public class InvalidTaskPriorityException extends RuntimeException {
    public InvalidTaskPriorityException(String message) {
        super(message);
    }
}