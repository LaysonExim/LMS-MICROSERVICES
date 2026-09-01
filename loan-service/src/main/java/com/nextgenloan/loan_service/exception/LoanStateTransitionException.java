package com.nextgenloan.loan_service.exception;

public class LoanStateTransitionException extends RuntimeException {
    public LoanStateTransitionException(String message) {
        super(message);
    }
}