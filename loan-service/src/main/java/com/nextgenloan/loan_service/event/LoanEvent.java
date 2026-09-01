// Purpose: Add versioning to event
// File: loan-service/src/main/java/com/nextgenloan/loan/event/LoanEvent.java

package com.nextgenloan.loan_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Loan Event - Version 2.0
 *
 * VERSION HISTORY:
 * - v1.0 (2026-01-01): Initial version with basic loan fields
 * - v2.0 (2026-07-10): Added loanPurpose, termMonths, interestRate, eventVersion
 * - v3.0 (Future): Will add additional fields
 *
 * BACKWARD COMPATIBILITY:
 * - All new fields are optional
 * - Consumers should ignore unknown fields
 * - Default values provided for missing fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEvent {

    @Builder.Default
    private String eventVersion = "2.0";

    // Event metadata
    private String eventType;
    private String correlationId;
    private LocalDateTime eventTimestamp;
    private String eventSource;

    // Core fields (v1.0)
    private String loanNumber;
    private String customerNumber;
    private BigDecimal amount;

    // New fields (v2.0) - all optional
    private String loanPurpose;      // NEW in v2.0
    private String loanType;         // NEW in v2.0
    private BigDecimal interestRate; // NEW in v2.0
    private Integer termMonths;      // NEW in v2.0

    // Customer details (denormalized)
    private String firstName;
    private String lastName;
    private String email;

    // Additional data (for extensibility)
    private String additionalData;

    /**
     * Check if this is a backward-compatible version
     */
    public boolean isBackwardCompatibleWith(String version) {
        // v2.0 is backward compatible with v1.0 (only added fields)
        if ("1.0".equals(version) && "2.0".equals(this.eventVersion)) {
            return true;
        }
        return this.eventVersion.equals(version);
    }
}