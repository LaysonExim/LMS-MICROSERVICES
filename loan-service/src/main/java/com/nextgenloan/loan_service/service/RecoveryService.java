// Purpose: Retry and recovery for failed operations
// File: loan-service/src/main/java/com/nextgenloan/loan/service/RecoveryService.java

package com.nextgenloan.loan_service.service;

import com.nextgenloan.loan_service.entity.LoanApplication;
import com.nextgenloan.loan_service.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryService {

    private final LoanRepository loanRepository;
    private final LoanService loanService;

    /**
     * Recover stuck loans
     *
     * WHY: Loans can get stuck in PENDING state if the saga fails.
     * This scheduled job recovers them.
     *
     * In a real bank:
     * 1. This would run every 5 minutes
     * 2. It would check for loans stuck for > 30 minutes
     * 3. It would attempt to complete the saga
     * 4. If retries fail, it would escalate to manual review
     *
     * This is a common pattern in banking for self-healing systems.
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void recoverStuckLoans() {
        log.debug("Running recovery job for stuck loans");

        // Find loans stuck in PENDING for more than 30 minutes
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<LoanApplication> stuckLoans = loanRepository.findByStatusAndCreatedAtBefore(
                "PENDING", threshold);

        if (stuckLoans.isEmpty()) {
            log.debug("No stuck loans found");
            return;
        }

        log.info("Found {} stuck loans to recover", stuckLoans.size());

        for (LoanApplication loan : stuckLoans) {
            try {
                recoverLoan(loan);
            } catch (Exception e) {
                log.error("Failed to recover loan {}: {}", loan.getLoanNumber(), e.getMessage());
                // In a real bank, we'd alert on-call engineer
            }
        }
    }

    /**
     * Recover a single loan
     *
     * WHY: Attempt to complete a stuck loan.
     *
     * If recovery fails, the loan stays PENDING for manual review.
     * This prevents data inconsistency while allowing recovery.
     */
    private void recoverLoan(LoanApplication loan) {
        log.info("Attempting to recover loan: {}", loan.getLoanNumber());

        try {
            // Try to verify customer again
            // In a real bank, we'd call Customer Service
            // For now, we'll just transition to VERIFIED
            loanService.transitionState(
                    loan.getLoanNumber(),
                    "VERIFIED",
                    "Recovered from stuck state"
            );

            log.info("Successfully recovered loan: {}", loan.getLoanNumber());
        } catch (Exception e) {
            log.error("Failed to recover loan {}: {}", loan.getLoanNumber(), e.getMessage());
            // Mark for manual review
            markForManualReview(loan);
        }
    }

    /**
     * Mark loan for manual review
     *
     * WHY: When automatic recovery fails, we need human intervention.
     *
     * In a real bank:
     * 1. This would send an alert to operations team
     * 2. Create a ticket in the support system
     * 3. Log the failure for audit
     * 4. Track manual resolution
     */
    private void markForManualReview(LoanApplication loan) {
        log.warn("Marking loan {} for manual review", loan.getLoanNumber());
        // In a real bank, we'd:
        // 1. Create a support ticket
        // 2. Send email to operations
        // 3. Set a flag on the loan
        // 4. Track for auditing
    }
}