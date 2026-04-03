package com.tattoo.scheduler.service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User with id " + userId + " not found");
    }
    public UserNotFoundException(String msg) {
        super(msg);
    }
}
