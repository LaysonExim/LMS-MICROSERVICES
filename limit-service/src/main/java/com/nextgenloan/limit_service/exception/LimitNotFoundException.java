package com.nextgenloan.limit_service.exception;

public class LimitNotFoundException extends RuntimeException {
    public LimitNotFoundException(String message) {
        super(message);
    }
}
