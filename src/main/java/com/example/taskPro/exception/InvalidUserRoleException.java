package com.example.taskPro.exception;

public class InvalidUserRoleException extends RuntimeException {
    public InvalidUserRoleException(String message) {
        super(message);
    }
}