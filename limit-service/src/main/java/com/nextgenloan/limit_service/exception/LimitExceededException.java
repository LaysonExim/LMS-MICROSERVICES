package com.nextgenloan.limit_service.exception;

public class LimitExceededException extends RuntimeException {
    public LimitExceededException(String message) {
        super(message);
    }
}
