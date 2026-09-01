// Purpose: Audit analytics endpoints
// File: audit-service/src/main/java/com/nextgenloan/audit/controller/AuditAnalyticsController.java

package com.nextgenloan.audit_service.controller;

import com.nextgenloan.audit_service.service.AuditAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Analytics", description = "Audit analytics and statistics")
public class AuditAnalyticsController {

    private final AuditAnalyticsService auditAnalyticsService;

    /**
     * Get audit statistics
     *
     * WHY: This provides a high-level view of system activity.
     *
     * In a real bank, this would be displayed on dashboards
     * and used for monitoring and compliance reporting.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get audit statistics",
            description = "Retrieves audit statistics for a date range")
    public ResponseEntity<Map<String, Object>> getAuditStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {

        log.info("Audit analytics from {} to {}", from, to);
        Map<String, Object> stats = auditAnalyticsService.getAuditStatistics(from, to);
        return ResponseEntity.ok(stats);
    }
}