// Purpose: Report API endpoints
// File: reporting-service/src/main/java/com/nextgenloan/reporting/controller/ReportingController.java

package com.nextgenloan.reporting_service.controller;

import com.nextgenloan.reporting_service.consumer.LoanEventConsumer;
import com.nextgenloan.reporting_service.model.ReportSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reporting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reporting", description = "API for report generation")
public class ReportingController {

    private final LoanEventConsumer loanEventConsumer;

    /**
     * Get report summary
     *
     * WHY: This provides a quick overview of the system.
     *
     * In a real bank, this would be used for:
     * 1. Management dashboards
     * 2. Daily reports
     * 3. System health monitoring
     * 4. Business intelligence
     */
    @GetMapping("/summary")
    @Operation(summary = "Get report summary",
            description = "Retrieves a summary of all loan activity")
    public ResponseEntity<ReportSummary> getReportSummary() {
        log.info("Reporting: Generating report summary");
        ReportSummary summary = loanEventConsumer.generateReportSummary();
        return ResponseEntity.ok(summary);
    }
}