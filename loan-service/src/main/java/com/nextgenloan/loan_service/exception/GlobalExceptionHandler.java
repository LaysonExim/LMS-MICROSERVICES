// Purpose: Global exception handler for Loan Service
// File: loan-service/src/main/java/com/nextgenloan/loan_service/exception/GlobalExceptionHandler.java

package com.nextgenloan.loan_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLoanNotFound(LoanNotFoundException ex) {
        Map<String, Object> response = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Loan Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidLoanOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidLoanOperation(InvalidLoanOperationException ex) {
        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Loan Operation",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(LoanStateTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleLoanStateTransition(LoanStateTransitionException ex) {
        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid State Transition",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid request parameters"
        );

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private Map<String, Object> createErrorResponse(int status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", status);
        response.put("error", error);
        response.put("message", message);
        response.put("path", "/api/v1/loans");
        return response;
    }
}
