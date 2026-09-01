// Purpose: Complete REST API for Loan Service
// File: loan-service/src/main/java/com/nextgenloan/loan/controller/LoanController.java

package com.nextgenloan.loan_service.controller;

import com.nextgenloan.loan_service.dto.*;
import com.nextgenloan.loan_service.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Management", description = "Complete API for managing loans")
public class LoanController {

    private final LoanService loanService;

    /**
     * Apply for a new loan
     *
     * HTTP: POST /api/v1/loans
     * Status: 201 Created
     */
    @PostMapping
    @Operation(summary = "Apply for a loan", description = "Submit a new loan application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan application created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public Mono<ResponseEntity<LoanResponseDto>> applyForLoan(
            @Valid @RequestBody LoanRequestDto requestDto) {
        log.info("POST /api/v1/loans - Processing loan application");
        return loanService.applyForLoan(requestDto)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Get loan by number
     *
     * HTTP: GET /api/v1/loans/{loanNumber}
     * Status: 200 OK
     */
    @GetMapping("/{loanNumber}")
    @Operation(summary = "Get loan by number", description = "Retrieves a loan by its unique loan number")
    public ResponseEntity<LoanResponseDto> getLoan(
            @Parameter(description = "Unique loan number", required = true)
            @PathVariable String loanNumber) {
        log.info("GET /api/v1/loans/{} - Retrieving loan", loanNumber);
        LoanResponseDto loan = loanService.getLoanByNumber(loanNumber);
        return ResponseEntity.ok(loan);
    }

    /**
     * Get loans by customer
     *
     * HTTP: GET /api/v1/loans/customer/{customerNumber}
     * Status: 200 OK
     */
    @GetMapping("/customer/{customerNumber}")
    @Operation(summary = "Get loans by customer", description = "Retrieves all loans for a customer")
    public ResponseEntity<List<LoanResponseDto>> getLoansByCustomer(
            @Parameter(description = "Customer number", required = true)
            @PathVariable String customerNumber) {
        log.info("GET /api/v1/loans/customer/{} - Retrieving loans", customerNumber);
        List<LoanResponseDto> loans = loanService.getLoansByCustomer(customerNumber);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get loans by customer with pagination
     *
     * HTTP: GET /api/v1/loans/customer/{customerNumber}?page=0&size=10
     */
    @GetMapping("/customer/{customerNumber}/page")
    @Operation(summary = "Get loans by customer (paginated)",
            description = "Retrieves paginated loans for a customer")
    public ResponseEntity<Page<LoanResponseDto>> getLoansByCustomerPaginated(
            @Parameter(description = "Customer number", required = true)
            @PathVariable String customerNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "applicationDate,desc") String sort) {
        log.info("GET /api/v1/loans/customer/{}/page - Retrieving paginated loans", customerNumber);
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<LoanResponseDto> loans = loanService.getLoansByCustomer(customerNumber, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get all loans with pagination
     *
     * HTTP: GET /api/v1/loans?page=0&size=20&sort=applicationDate,desc
     */
    @GetMapping
    @Operation(summary = "Get all loans", description = "Retrieves paginated list of all loans")
    public ResponseEntity<Page<LoanResponseDto>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "applicationDate,desc") String sort) {
        log.info("GET /api/v1/loans - Retrieving all loans");
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<LoanResponseDto> loans = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get loans by status
     *
     * HTTP: GET /api/v1/loans/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get loans by status", description = "Retrieves all loans with a specific status")
    public ResponseEntity<List<LoanResponseDto>> getLoansByStatus(
            @Parameter(description = "Loan status", required = true,
                    example = "PENDING, VERIFIED, APPROVED, ACTIVE, CLOSED, REJECTED")
            @PathVariable String status) {
        log.info("GET /api/v1/loans/status/{} - Retrieving loans", status);
        List<LoanResponseDto> loans = loanService.getLoansByStatus(status);
        return ResponseEntity.ok(loans);
    }

    /**
     * Update loan status
     *
     * HTTP: PATCH /api/v1/loans/{loanNumber}/status?status=APPROVED
     * Status: 200 OK
     */
    @PatchMapping("/{loanNumber}/status")
    @Operation(summary = "Update loan status", description = "Transitions loan to a new state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid transition"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<LoanResponseDto> updateStatus(
            @Parameter(description = "Unique loan number", required = true)
            @PathVariable String loanNumber,
            @Parameter(description = "Target status", required = true,
                    example = "VERIFIED, APPROVED, ACTIVE, CLOSED, REJECTED")
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "Status update") String reason) {
        log.info("PATCH /api/v1/loans/{}/status - Updating status to: {}", loanNumber, status);
        LoanResponseDto updatedLoan = loanService.transitionState(loanNumber, status, reason);
        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Get loan schedule
     *
     * HTTP: GET /api/v1/loans/{loanNumber}/schedule
     */
    @GetMapping("/{loanNumber}/schedule")
    @Operation(summary = "Get loan schedule", description = "Retrieves the repayment schedule for a loan")
    public ResponseEntity<List<LoanScheduleDto>> getSchedule(
            @Parameter(description = "Unique loan number", required = true)
            @PathVariable String loanNumber) {
        log.info("GET /api/v1/loans/{}/schedule - Retrieving schedule", loanNumber);
        List<LoanScheduleDto> schedules = loanService.getLoanSchedule(loanNumber);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Record repayment
     *
     * HTTP: POST /api/v1/loans/{loanNumber}/repayments
     * Status: 201 Created
     */
    @PostMapping("/{loanNumber}/repayments")
    @Operation(summary = "Record repayment", description = "Records a loan repayment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Repayment recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<LoanRepaymentDto> recordRepayment(
            @Parameter(description = "Unique loan number", required = true)
            @PathVariable String loanNumber,
            @Valid @RequestBody LoanRepaymentRequestDto requestDto) {
        log.info("POST /api/v1/loans/{}/repayments - Recording repayment", loanNumber);
        LoanRepaymentDto repayment = loanService.recordRepayment(loanNumber, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(repayment);
    }

    /**
     * Get loan repayments
     *
     * HTTP: GET /api/v1/loans/{loanNumber}/repayments
     */
    @GetMapping("/{loanNumber}/repayments")
    @Operation(summary = "Get loan repayments", description = "Retrieves all repayments for a loan")
    public ResponseEntity<List<LoanRepaymentDto>> getRepayments(
            @Parameter(description = "Unique loan number", required = true)
            @PathVariable String loanNumber) {
        log.info("GET /api/v1/loans/{}/repayments - Retrieving repayments", loanNumber);
        List<LoanRepaymentDto> repayments = loanService.getLoanRepayments(loanNumber);
        return ResponseEntity.ok(repayments);
    }

    /**
     * Parse sort parameter
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by("applicationDate").descending();
        }
        String[] parts = sort.split(",");
        if (parts.length == 1) {
            return Sort.by(parts[0]).descending();
        }
        String field = parts[0];
        String direction = parts[1];
        return direction.equalsIgnoreCase("asc")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
    }
}