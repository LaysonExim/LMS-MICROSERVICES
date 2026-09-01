// Purpose: Consumes events for reporting
// File: reporting-service/src/main/java/com/nextgenloan/reporting/consumer/LoanEventConsumer.java

package com.nextgenloan.reporting_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextgenloan.reporting_service.dto.LoanEvent;
import com.nextgenloan.reporting_service.model.LoanReportData;
import com.nextgenloan.reporting_service.model.ReportSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanEventConsumer {

    private final ObjectMapper objectMapper;

    // In-memory report data (in production, this would be stored in a database)
    private final Map<String, LoanReportData> loanReportData = new ConcurrentHashMap<>();

    /**
     * Consume loan events and update reporting data
     *
     * WHY: Reports need to be updated in real-time.
     *
     * This service maintains aggregated data for reports:
     * - Total loans per customer
     * - Total amount per loan type
     * - Average interest rate
     * - Loan status distribution
     *
     * In a real bank, this would be:
     * 1. Stored in a data warehouse
     * 2. Updated in real-time
     * 3. Queryable for reporting
     * 4. Used for business intelligence
     */
    @KafkaListener(topics = "loan-events", groupId = "reporting-service-group")
    public void consumeLoanEvent(String eventJson) {
        try {
            LoanEvent event = objectMapper.readValue(eventJson, LoanEvent.class);

            log.info("Reporting: Received event: {} for loan: {}",
                    event.getEventType(), event.getLoanNumber());

            // Update report data based on event type
            switch (event.getEventType()) {
                case "LOAN_CREATED":
                    handleLoanCreated(event);
                    break;
                case "LOAN_STATUS_CHANGED":
                    handleLoanStatusChanged(event);
                    break;
                case "LOAN_DISBURSED":
                    handleLoanDisbursed(event);
                    break;
                default:
                    log.debug("Reporting: No action for event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Reporting: Failed to process event: {}", e.getMessage(), e);
        }
    }

    private void handleLoanCreated(LoanEvent event) {
        LoanReportData data = LoanReportData.builder()
                .loanNumber(event.getLoanNumber())
                .customerNumber(event.getCustomerNumber())
                .loanType(event.getLoanType())
                .amount(event.getAmount())
                .interestRate(event.getInterestRate())
                .termMonths(event.getTermMonths())
                .status("PENDING")
                .createdAt(event.getEventTimestamp())
                .build();

        loanReportData.put(event.getLoanNumber(), data);

        log.info("Reporting: Loan report data created for: {}", event.getLoanNumber());

        // Generate report summary
        generateReportSummary();
    }

    private void handleLoanStatusChanged(LoanEvent event) {
        LoanReportData data = loanReportData.get(event.getLoanNumber());
        if (data != null) {
            data.setStatus(extractNewStatus(event.getAdditionalData()));
            data.setUpdatedAt(event.getEventTimestamp());
            log.info("Reporting: Loan status updated: {} → {}",
                    event.getLoanNumber(), data.getStatus());
        }
    }

    private void handleLoanDisbursed(LoanEvent event) {
        LoanReportData data = loanReportData.get(event.getLoanNumber());
        if (data != null) {
            data.setStatus("DISBURSED");
            data.setDisbursedAt(event.getEventTimestamp());
            log.info("Reporting: Loan disbursed: {}", event.getLoanNumber());
        }
    }

    /**
     * Generate report summary
     *
     * WHY: This provides aggregated reporting data.
     * In a real bank, this would be exposed via REST API.
     */
    public ReportSummary generateReportSummary() {
        long totalLoans = loanReportData.size();
        BigDecimal totalAmount = loanReportData.values().stream()
                .map(LoanReportData::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> loansByType = new ConcurrentHashMap<>();
        Map<String, Long> loansByStatus = new ConcurrentHashMap<>();

        for (LoanReportData data : loanReportData.values()) {
            loansByType.merge(data.getLoanType(), 1L, Long::sum);
            loansByStatus.merge(data.getStatus(), 1L, Long::sum);
        }

        return ReportSummary.builder()
                .totalLoans(totalLoans)
                .totalAmount(totalAmount)
                .loansByType(loansByType)
                .loansByStatus(loansByStatus)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private String extractNewStatus(String additionalData) {
        // Parse newStatus from additionalData
        // In a real implementation, this would be more robust
        if (additionalData != null && additionalData.contains("newStatus=")) {
            String[] parts = additionalData.split(",");
            for (String part : parts) {
                if (part.startsWith("newStatus=")) {
                    return part.substring(10);
                }
            }
        }
        return "UNKNOWN";
    }
}