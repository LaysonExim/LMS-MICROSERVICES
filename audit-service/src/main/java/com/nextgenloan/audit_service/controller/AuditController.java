// Purpose: REST API for audit events
// File: audit-service/src/main/java/com/nextgenloan/audit/controller/AuditController.java

package com.nextgenloan.audit_service.controller;

import com.nextgenloan.audit_service.entity.AuditEvent;
import com.nextgenloan.audit_service.repository.AuditEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit", description = "API for audit trail queries")
public class AuditController {

    private final AuditEventRepository auditRepository;

    /**
     * Get audit events for a specific loan
     *
     * WHY: This is the primary query for regulators and auditors.
     * They need to see all activities for a specific loan.
     *
     * In a real bank, this would be heavily secured.
     */
    @GetMapping("/loan/{loanNumber}")
    @Operation(summary = "Get audit events for loan",
            description = "Retrieves all audit events for a specific loan")
    public ResponseEntity<List<AuditEvent>> getEventsForLoan(
            @PathVariable String loanNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Audit query for loan: {}", loanNumber);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("eventTimestamp").descending());

        Page<AuditEvent> events = auditRepository
                .findByLoanNumber(loanNumber, pageable);

        return ResponseEntity.ok(events.getContent());
    }

    /**
     * Get audit events for a specific customer
     */
    @GetMapping("/customer/{customerNumber}")
    @Operation(summary = "Get audit events for customer",
            description = "Retrieves all audit events for a specific customer")
    public ResponseEntity<List<AuditEvent>> getEventsForCustomer(
            @PathVariable String customerNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Audit query for customer: {}", customerNumber);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("eventTimestamp").descending());

        Page<AuditEvent> events = auditRepository
                .findByCustomerNumber(customerNumber, pageable);

        return ResponseEntity.ok(events.getContent());
    }

    /**
     * Get audit events by event type
     */
    @GetMapping("/type/{eventType}")
    @Operation(summary = "Get audit events by type",
            description = "Retrieves audit events by event type")
    public ResponseEntity<List<AuditEvent>> getEventsByType(
            @PathVariable String eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Audit query for type: {}", eventType);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("eventTimestamp").descending());

        Page<AuditEvent> events = auditRepository
                .findByEventType(eventType, pageable);

        return ResponseEntity.ok(events.getContent());
    }

    /**
     * Get audit events by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get audit events by date range",
            description = "Retrieves audit events between two dates")
    public ResponseEntity<List<AuditEvent>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Audit query from {} to {}", from, to);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("eventTimestamp").descending());

        Page<AuditEvent> events = auditRepository
                .findByEventTimestampBetween(from, to, pageable);

        return ResponseEntity.ok(events.getContent());
    }
}