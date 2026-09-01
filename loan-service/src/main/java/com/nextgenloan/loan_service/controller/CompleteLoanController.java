package com.nextgenloan.loan_service.controller;

import com.nextgenloan.loan_service.dto.CompleteLoanRequestDto;
import com.nextgenloan.loan_service.dto.LoanRequestDto;
import com.nextgenloan.loan_service.dto.LoanResponseDto;
import com.nextgenloan.loan_service.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/loans/complete")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Complete Loan Flow", description = "End-to-end loan application with all integrations")
public class CompleteLoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Apply for loan (complete flow)",
            description = "Processes loan application with all service integrations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Conflict (insufficient credit or collateral)")
    })
    public Mono<ResponseEntity<LoanResponseDto>> applyForLoanComplete(
            @Valid @RequestBody CompleteLoanRequestDto requestDto) {
        log.info("POST /api/v1/loans/complete - Processing complete loan flow");

        LoanRequestDto loanRequest = LoanRequestDto.builder()
                .customerNumber(requestDto.getCustomerNumber())
                .loanType(requestDto.getLoanType())
                .loanPurpose(requestDto.getLoanPurpose())
                .amount(requestDto.getAmount())
                .interestRate(requestDto.getInterestRate())
                .termMonths(requestDto.getTermMonths())
                .build();

        return loanService.applyForLoanComplete(loanRequest)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
}
