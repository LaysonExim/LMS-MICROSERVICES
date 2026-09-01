package com.nextgenloan.collateral_service.controller;

import com.nextgenloan.collateral_service.dto.CollateralRequestDto;
import com.nextgenloan.collateral_service.dto.CollateralResponseDto;
import com.nextgenloan.collateral_service.dto.CollateralValidationResponse;
import com.nextgenloan.collateral_service.service.CollateralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/collateral")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Collateral Management", description = "API for managing collateral assets")
public class CollateralController {

    private final CollateralService collateralService;

    @PostMapping
    @Operation(summary = "Register collateral", description = "Registers a new collateral asset")
    public ResponseEntity<CollateralResponseDto> registerCollateral(
            @Valid @RequestBody CollateralRequestDto requestDto) {
        log.info("POST /api/v1/collateral - Registering collateral");
        CollateralResponseDto response = collateralService.registerCollateral(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{collateralReference}/link")
    @Operation(summary = "Link collateral to loan", description = "Links collateral to a loan")
    public ResponseEntity<CollateralResponseDto> linkCollateralToLoan(
            @PathVariable String collateralReference,
            @RequestParam String loanNumber) {
        log.info("POST /api/v1/collateral/{}/link - Linking to loan: {}",
                collateralReference, loanNumber);
        CollateralResponseDto response = collateralService.linkCollateralToLoan(
                collateralReference, loanNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{collateralReference}/release")
    @Operation(summary = "Release collateral", description = "Releases collateral from a loan")
    public ResponseEntity<CollateralResponseDto> releaseCollateral(
            @PathVariable String collateralReference) {
        log.info("POST /api/v1/collateral/{}/release - Releasing", collateralReference);
        CollateralResponseDto response = collateralService.releaseCollateral(collateralReference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate collateral", description = "Validates collateral for a loan")
    public ResponseEntity<CollateralValidationResponse> validateCollateral(
            @RequestParam String customerNumber,
            @RequestParam String loanNumber,
            @RequestParam BigDecimal loanAmount) {
        log.info("GET /api/v1/collateral/validate - Validating for customer: {}",
                customerNumber);
        CollateralValidationResponse response = collateralService.validateCollateral(
                customerNumber, loanNumber, loanAmount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{collateralReference}")
    @Operation(summary = "Get collateral by reference",
            description = "Retrieves collateral by its reference")
    public ResponseEntity<CollateralResponseDto> getByReference(
            @PathVariable String collateralReference) {
        log.info("GET /api/v1/collateral/{} - Retrieving", collateralReference);
        CollateralResponseDto response = collateralService.getByReference(collateralReference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerNumber}")
    @Operation(summary = "Get collateral by customer",
            description = "Retrieves all collateral for a customer")
    public ResponseEntity<List<CollateralResponseDto>> getByCustomer(
            @PathVariable String customerNumber) {
        log.info("GET /api/v1/collateral/customer/{} - Retrieving", customerNumber);
        List<CollateralResponseDto> response = collateralService.getByCustomer(customerNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/loan/{loanNumber}")
    @Operation(summary = "Get collateral by loan",
            description = "Retrieves all collateral linked to a loan")
    public ResponseEntity<List<CollateralResponseDto>> getByLoan(
            @PathVariable String loanNumber) {
        log.info("GET /api/v1/collateral/loan/{} - Retrieving", loanNumber);
        List<CollateralResponseDto> response = collateralService.getByLoan(loanNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/loan/{loanNumber}/link")
    @Operation(summary = "Link collateral to loan by loan number",
            description = "Links all active collateral for a customer's loan")
    public ResponseEntity<Void> linkCollateralToLoanByLoanNumber(
            @PathVariable String loanNumber) {
        log.info("POST /api/v1/collateral/loan/{}/link - Linking collateral to loan", loanNumber);
        collateralService.linkCollateralToLoan(loanNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/link-all/{loanNumber}")
    @Operation(summary = "Link all collateral",
            description = "Links all active collateral for the customer to a loan")
    public ResponseEntity<Void> linkAllCollateralToLoan(
            @PathVariable String loanNumber) {
        log.info("POST /api/v1/collateral/link-all/{} - Linking all collateral", loanNumber);
        collateralService.linkAllCollateralToLoan(loanNumber);
        return ResponseEntity.ok().build();
    }
}