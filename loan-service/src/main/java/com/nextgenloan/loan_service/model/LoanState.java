// Purpose: Loan state enumeration with transition rules
// File: loan-service/src/main/java/com/nextgenloan/loan/model/LoanState.java

package com.nextgenloan.loan_service.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Loan State Enum
 *
 * WHY: In banking, loan state management is critical.
 * Each state has specific rules and allowed transitions.
 *
 * This enum encapsulates the state machine logic:
 * 1. What states exist
 * 2. What transitions are allowed
 * 3. What actions trigger transitions
 *
 * In a real bank, there would be more states and more complex rules.
 * This is a simplified version for learning.
 */
@Getter
public enum LoanState {

    /**
     * PENDING - Initial state after application submission
     * Allowed transitions: VERIFIED, REJECTED
     */
    PENDING("PENDING", "Loan application submitted, pending verification",
            Set.of("VERIFIED", "REJECTED")),

    /**
     * VERIFIED - Customer information verified
     * Allowed transitions: APPROVED, REJECTED
     */
    VERIFIED("VERIFIED", "Customer information verified, pending approval",
            Set.of("APPROVED", "REJECTED")),

    /**
     * APPROVED - Loan approved, pending disbursement
     * Allowed transitions: ACTIVE, REJECTED
     */
    APPROVED("APPROVED", "Loan approved, pending disbursement",
            Set.of("ACTIVE", "REJECTED")),

    /**
     * ACTIVE - Loan disbursed, being repaid
     * Allowed transitions: CLOSED
     */
    ACTIVE("ACTIVE", "Loan active, being repaid",
            Set.of("CLOSED")),

    /**
     * CLOSED - Loan fully repaid and closed
     * Allowed transitions: None (terminal state)
     */
    CLOSED("CLOSED", "Loan fully repaid and closed",
            Set.of()),

    /**
     * REJECTED - Loan application rejected
     * Allowed transitions: None (terminal state)
     */
    REJECTED("REJECTED", "Loan application rejected",
            Set.of());

    private final String code;
    private final String description;
    private final Set<String> allowedTransitions;

    LoanState(String code, String description, Set<String> allowedTransitions) {
        this.code = code;
        this.description = description;
        this.allowedTransitions = allowedTransitions;
    }

    /**
     * Check if transition is allowed
     */
    public boolean canTransitionTo(String targetState) {
        return allowedTransitions.contains(targetState);
    }

    /**
     * Get state by code
     */
    public static LoanState fromCode(String code) {
        return Arrays.stream(values())
                .filter(state -> state.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid state: " + code));
    }

    /**
     * Check if state is terminal (cannot transition to any other state)
     */
    public boolean isTerminal() {
        return allowedTransitions.isEmpty();
    }

    /**
     * Get list of terminal states
     */
    public static List<LoanState> getTerminalStates() {
        return Arrays.stream(values())
                .filter(LoanState::isTerminal)
                .toList();
    }
}