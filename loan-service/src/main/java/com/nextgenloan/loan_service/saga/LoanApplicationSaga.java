// Purpose: Saga coordinator for loan application
// File: loan-service/src/main/java/com/nextgenloan/loan/saga/LoanApplicationSaga.java
// Dependencies: LoanService, CustomerServiceClient

package com.nextgenloan.loan_service.saga;

import com.nextgenloan.loan_service.client.CustomerServiceClient;
import com.nextgenloan.loan_service.dto.CustomerDto;
import com.nextgenloan.loan_service.dto.LoanRequestDto;
import com.nextgenloan.loan_service.dto.LoanResponseDto;
import com.nextgenloan.loan_service.entity.LoanApplication;
import com.nextgenloan.loan_service.exception.InvalidLoanOperationException;
import com.nextgenloan.loan_service.model.LoanState;
import com.nextgenloan.loan_service.repository.LoanRepository;
import com.nextgenloan.loan_service.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Loan Application Saga
 *
 * WHY: This orchestrates the distributed transaction for loan applications.
 *
 * In a real bank, a loan application would involve multiple services:
 * 1. Customer Service - Verify customer
 * 2. Limit Service - Check credit limit
 * 3. Collateral Service - Check collateral
 * 4. Loan Service - Create loan
 * 5. Notification Service - Send notifications
 *
 * For our learning, we focus on the core flow with Customer Service.
 *
 * SAGA PATTERN:
 * - Each step has a compensating action
 * - If any step fails, we execute compensations
 * - The saga tracks state and can be retried
 *
 * In a real bank, this would be implemented with:
 * - A distributed saga framework (like Camunda or Axon)
 * - Event-driven communication
 * - Persistent saga state (for recovery)
 * - Monitoring and alerting
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationSaga {

    private final LoanService loanService;
    private final CustomerServiceClient customerServiceClient;
    private final LoanRepository loanRepository;

    // In-memory saga state (in production, this would be persistent)
    private final Map<String, SagaState> sagaStates = new HashMap<>();

    /**
     * Execute loan application saga
     *
     * WHY: This is the entry point for the saga.
     *
     * STEPS:
     * 1. START: Create loan (PENDING)
     * 2. VERIFY: Validate customer
     * 3. COMPLETE: Transition loan to VERIFIED
     * 4. On failure: Execute compensating actions
     *
     * In a real bank, there would be more steps:
     * - Credit check
     * - Limit check
     * - Collateral check
     * - Approval workflow
     * - Disbursement
     */
    public Mono<LoanResponseDto> executeSaga(LoanRequestDto requestDto) {
        String sagaId = generateSagaId();
        log.info("Starting loan application saga: {}", sagaId);

        // Initialize saga state
        SagaState state = SagaState.builder()
                .sagaId(sagaId)
                .customerNumber(requestDto.getCustomerNumber())
                .loanNumber(null)
                .status(SagaStatus.STARTED)
                .startedAt(LocalDateTime.now())
                .build();

        sagaStates.put(sagaId, state);

        // Step 1: Create loan (PENDING)
        return createLoan(requestDto, state)
                // Step 2: Verify customer
                .flatMap(loan -> verifyCustomer(loan, state))
                // Step 3: Complete saga
                .flatMap(loan -> completeSaga(loan, state))
                // Handle failures
                .onErrorResume(error -> handleSagaFailure(state, error));
    }

    /**
     * Step 1: Create loan
     *
     * WHY: Create the loan in PENDING state.
     *
     * This is the first step in the saga.
     * If this fails, no data has been created.
     */
    private Mono<LoanResponseDto> createLoan(LoanRequestDto requestDto, SagaState state) {
        log.info("Saga {}: Step 1 - Creating loan", state.getSagaId());

        return Mono.fromCallable(() -> {
            // Create loan using existing service logic
            LoanResponseDto loan = loanService.createLoanApplication(requestDto);
            state.setLoanNumber(loan.getLoanNumber());
            state.setStatus(SagaStatus.LOAN_CREATED);
            sagaStates.put(state.getSagaId(), state);
            log.info("Saga {}: Loan created: {}", state.getSagaId(), loan.getLoanNumber());
            return loan;
        });
    }

    /**
     * Step 2: Verify customer
     *
     * WHY: Verify the customer exists and is active.
     *
     * This is a critical step in banking:
     * - Customer must exist
     * - Customer must be active
     * - Customer must be eligible for loans
     *
     * If this fails, we need to roll back the loan creation.
     */
    private Mono<LoanResponseDto> verifyCustomer(LoanResponseDto loan, SagaState state) {
        log.info("Saga {}: Step 2 - Verifying customer: {}", state.getSagaId(), state.getCustomerNumber());

        return customerServiceClient.validateCustomer(state.getCustomerNumber())
                .flatMap(isValid -> {
                    if (!isValid) {
                        log.error("Saga {}: Customer verification failed", state.getSagaId());
                        return Mono.error(new InvalidLoanOperationException(
                                "Customer verification failed for: " + state.getCustomerNumber()
                        ));
                    }

                    state.setStatus(SagaStatus.CUSTOMER_VERIFIED);
                    sagaStates.put(state.getSagaId(), state);
                    log.info("Saga {}: Customer verified successfully", state.getSagaId());
                    return Mono.just(loan);
                });
    }

    /**
     * Step 3: Complete saga
     *
     * WHY: Transition loan to VERIFIED state.
     *
     * If all steps succeed, we transition the loan.
     * This is the "commit" step of the saga.
     */
    private Mono<LoanResponseDto> completeSaga(LoanResponseDto loan, SagaState state) {
        log.info("Saga {}: Step 3 - Completing saga", state.getSagaId());

        return Mono.fromCallable(() -> {
            // Transition loan to VERIFIED
            LoanResponseDto updatedLoan = loanService.transitionState(
                    loan.getLoanNumber(),
                    LoanState.VERIFIED.getCode(),
                    "Saga completed successfully"
            );

            state.setStatus(SagaStatus.COMPLETED);
            state.setCompletedAt(LocalDateTime.now());
            sagaStates.put(state.getSagaId(), state);
            log.info("Saga {}: Completed successfully", state.getSagaId());

            return updatedLoan;
        });
    }

    /**
     * Handle saga failure
     *
     * WHY: When any step fails, we need to execute compensations.
     *
     * This is the "rollback" part of the saga.
     *
     * In a real bank, this would involve:
     * 1. Reversing any successful steps
     * 2. Logging the failure
     * 3. Triggering alerts
     * 4. Queueing for manual review
     */
    private Mono<LoanResponseDto> handleSagaFailure(SagaState state, Throwable error) {
        log.error("Saga {}: Failed - {}", state.getSagaId(), error.getMessage());

        state.setStatus(SagaStatus.FAILED);
        state.setError(error.getMessage());
        state.setFailedAt(LocalDateTime.now());
        sagaStates.put(state.getSagaId(), state);

        // Execute compensating actions
        return executeCompensations(state)
                .then(Mono.error(error));
    }

    /**
     * Execute compensating actions
     *
     * WHY: Reversing successful steps to maintain consistency.
     *
     * In our case, if customer verification fails,
     * we need to cancel the loan.
     *
     * In a real bank, we would:
     * 1. Cancel the loan
     * 2. Release any reserved limits
     * 3. Release any held funds
     * 4. Log the compensation
     */
    private Mono<Void> executeCompensations(SagaState state) {
        log.info("Saga {}: Executing compensations", state.getSagaId());

        return Mono.fromRunnable(() -> {
            // If loan was created, cancel it
            if (state.getLoanNumber() != null &&
                    state.getStatus().ordinal() >= SagaStatus.LOAN_CREATED.ordinal()) {
                try {
                    loanService.transitionState(
                            state.getLoanNumber(),
                            LoanState.REJECTED.getCode(),
                            "Saga failed: " + state.getError()
                    );
                    log.info("Saga {}: Loan cancelled: {}", state.getSagaId(), state.getLoanNumber());
                } catch (Exception e) {
                    log.error("Saga {}: Failed to cancel loan: {}", state.getSagaId(), e.getMessage());
                    // In a real bank, we'd retry or escalate
                }
            }

            state.setStatus(SagaStatus.COMPENSATED);
            state.setCompensatedAt(LocalDateTime.now());
            sagaStates.put(state.getSagaId(), state);
            log.info("Saga {}: Compensations completed", state.getSagaId());
        });
    }

    /**
     * Generate saga ID
     */
    private String generateSagaId() {
        return "SAGA-" + System.currentTimeMillis() + "-" +
                String.format("%04d", (long) (Math.random() * 10000));
    }

    /**
     * Get saga state
     */
    public SagaState getSagaState(String sagaId) {
        return sagaStates.get(sagaId);
    }

    /**
     * Saga Status Enum
     */
    public enum SagaStatus {
        STARTED,
        LOAN_CREATED,
        CUSTOMER_VERIFIED,
        COMPLETED,
        FAILED,
        COMPENSATED
    }

    /**
     * Saga State Class
     */
    @lombok.Data
    @lombok.Builder
    public static class SagaState {
        private String sagaId;
        private String customerNumber;
        private String loanNumber;
        private SagaStatus status;
        private String error;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime failedAt;
        private LocalDateTime compensatedAt;
    }
}