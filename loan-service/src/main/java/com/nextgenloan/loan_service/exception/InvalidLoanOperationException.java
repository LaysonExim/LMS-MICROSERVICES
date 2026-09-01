package com.nextgenloan.loan_service.exception;

public class InvalidLoanOperationException extends RuntimeException {
    public InvalidLoanOperationException(String message) {
        super(message);
    }
}