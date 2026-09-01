// Purpose: REST API for saga operations
// File: loan-service/src/main/java/com/nextgenloan/loan/controller/SagaController.java

package com.nextgenloan.loan_service.controller;

import com.nextgenloan.loan_service.dto.LoanRequestDto;
import com.nextgenloan.loan_service.dto.LoanResponseDto;
import com.nextgenloan.loan_service.saga.LoanApplicationSaga;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/saga/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Saga Management", description = "API for distributed transaction sagas")
public class SagaController {

    private final LoanApplicationSaga loanApplicationSaga;

    /**
     * Apply for a loan using saga pattern
     *
     * WHY: This uses the saga pattern for distributed transactions.
     *
     * PROCESS:
     * 1. Loan is created (PENDING)
     * 2. Customer is verified
     * 3. Loan is transitioned to VERIFIED
     * 4. If any step fails, compensations are executed
     *
     * This ensures consistency even with failures.
     *
     * HTTP: POST /api/v1/saga/loans
     * Status: 202 Accepted (async)
     */
    @PostMapping
    @Operation(summary = "Apply for loan (saga)",
            description = "Submits loan application using distributed transaction saga")
    public Mono<ResponseEntity<LoanResponseDto>> applyForLoanSaga(
            @Valid @RequestBody LoanRequestDto requestDto) {
        log.info("POST /api/v1/saga/loans - Processing loan via saga");

        return loanApplicationSaga.executeSaga(requestDto)
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response))
                .onErrorResume(error -> {
                    log.error("Saga failed: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
                });
    }

    /**
     * Get saga state
     *
     * WHY: Monitor the status of a distributed transaction.
     *
     * In a real bank, this would be used for:
     * 1. Monitoring - See pending sagas
     * 2. Troubleshooting - Debug failed sagas
     * 3. Recovery - Retry failed sagas
     * 4. Auditing - Track distributed transactions
     *
     * HTTP: GET /api/v1/saga/loans/{sagaId}
     * Status: 200 OK
     */
    @GetMapping("/{sagaId}")
    @Operation(summary = "Get saga state",
            description = "Retrieves the current state of a saga")
    public ResponseEntity<LoanApplicationSaga.SagaState> getSagaState(
            @PathVariable String sagaId) {
        log.info("GET /api/v1/saga/loans/{} - Retrieving saga state", sagaId);
        LoanApplicationSaga.SagaState state = loanApplicationSaga.getSagaState(sagaId);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }
}