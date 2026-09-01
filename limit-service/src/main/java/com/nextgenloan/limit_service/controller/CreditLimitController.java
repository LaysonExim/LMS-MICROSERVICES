// Purpose: REST API for credit limits
// File: limit-service/src/main/java/com/nextgenloan/limit/controller/CreditLimitController.java

package com.nextgenloan.limit_service.controller;

import com.nextgenloan.limit_service.dto.CreditLimitRequestDto;
import com.nextgenloan.limit_service.dto.CreditLimitResponseDto;
import com.nextgenloan.limit_service.dto.LimitCheckResponseDto;
import com.nextgenloan.limit_service.entity.LimitReservation;
import com.nextgenloan.limit_service.service.CreditLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credit-limits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Credit Limit Management", description = "API for managing customer credit limits")
public class CreditLimitController {

    private final CreditLimitService creditLimitService;

    /**
     * Check and reserve credit
     *
     * HTTP: POST /api/v1/credit-limits/check
     * Status: 200 OK
     */
    @PostMapping("/check")
    @Operation(summary = "Check and reserve credit",
            description = "Checks if credit is available and reserves it for loan application")
    public ResponseEntity<LimitCheckResponseDto> checkAndReserveCredit(
            @Valid @RequestBody CreditLimitRequestDto requestDto) {
        log.info("POST /api/v1/credit-limits/check - Checking credit for: {}",
                requestDto.getCustomerNumber());
        LimitCheckResponseDto response = creditLimitService.checkAndReserveCredit(requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm reservation
     *
     * HTTP: POST /api/v1/credit-limits/reservations/{reservationId}/confirm
     * Status: 200 OK
     */
    @PostMapping("/reservations/{reservationId}/confirm")
    @Operation(summary = "Confirm reservation",
            description = "Confirms a credit reservation (permanent deduction)")
    public ResponseEntity<Void> confirmReservation(@PathVariable String reservationId) {
        log.info("POST /api/v1/credit-limits/reservations/{}/confirm - Confirming", reservationId);
        creditLimitService.confirmReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    /**
     * Release reservation
     *
     * HTTP: POST /api/v1/credit-limits/reservations/{reservationId}/release
     * Status: 200 OK
     */
    @PostMapping("/reservations/{reservationId}/release")
    @Operation(summary = "Release reservation",
            description = "Releases a credit reservation (for cancelled/rejected loans)")
    public ResponseEntity<Void> releaseReservation(@PathVariable String reservationId) {
        log.info("POST /api/v1/credit-limits/reservations/{}/release - Releasing", reservationId);
        creditLimitService.releaseReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get reservation
     *
     * HTTP: GET /api/v1/credit-limits/reservations/{reservationId}
     * Status: 200 OK
     */
    @GetMapping("/reservations/{reservationId}")
    @Operation(summary = "Get reservation",
            description = "Retrieves a credit reservation by ID")
    public ResponseEntity<LimitReservation> getReservation(@PathVariable String reservationId) {
        log.info("GET /api/v1/credit-limits/reservations/{} - Retrieving", reservationId);
        LimitReservation reservation = creditLimitService.getReservation(reservationId);
        return ResponseEntity.ok(reservation);
    }

    /**
     * Get customer limit
     *
     * HTTP: GET /api/v1/credit-limits/customer/{customerNumber}
     * Status: 200 OK
     */
    @GetMapping("/customer/{customerNumber}")
    @Operation(summary = "Get customer limit",
            description = "Retrieves credit limits for a specific customer")
    public ResponseEntity<CreditLimitResponseDto> getCustomerLimit(
            @PathVariable String customerNumber) {
        log.info("GET /api/v1/credit-limits/customer/{} - Retrieving", customerNumber);
        CreditLimitResponseDto limit = creditLimitService.getCustomerLimit(customerNumber);
        return ResponseEntity.ok(limit);
    }
}