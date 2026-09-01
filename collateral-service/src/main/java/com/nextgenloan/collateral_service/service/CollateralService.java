// Purpose: Collateral business logic
// File: collateral-service/src/main/java/com/nextgenloan/collateral/service/CollateralService.java

package com.nextgenloan.collateral_service.service;

import com.nextgenloan.collateral_service.dto.CollateralRequestDto;
import com.nextgenloan.collateral_service.dto.CollateralResponseDto;
import com.nextgenloan.collateral_service.dto.CollateralValidationResponse;
import com.nextgenloan.collateral_service.entity.CollateralAsset;
import com.nextgenloan.collateral_service.entity.CollateralValuation;
import com.nextgenloan.collateral_service.exception.CollateralNotFoundException;
import com.nextgenloan.collateral_service.exception.CollateralValidationException;
import com.nextgenloan.collateral_service.mapper.CollateralMapper;
import com.nextgenloan.collateral_service.repository.CollateralAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollateralService {

    private final CollateralAssetRepository collateralRepository;
    private final CollateralMapper collateralMapper;

    /**
     * Register new collateral
     */
    @Transactional
    public CollateralResponseDto registerCollateral(CollateralRequestDto requestDto) {
        log.info("Registering collateral for customer: {}", requestDto.getCustomerNumber());

        // Generate collateral reference
        String collateralReference = generateCollateralReference();

        CollateralAsset collateral = CollateralAsset.builder()
                .collateralReference(collateralReference)
                .customerNumber(requestDto.getCustomerNumber())
                .assetType(requestDto.getAssetType())
                .assetName(requestDto.getAssetName())
                .assetDescription(requestDto.getAssetDescription())
                .valuation(requestDto.getValuation())
                .valuationDate(requestDto.getValuationDate())
                .status("ACTIVE")
                .legalStatus("PENDING")
                .insuranceStatus("PENDING")
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .build();

        // If loan number is provided, link the collateral
        if (requestDto.getLoanNumber() != null) {
            collateral.setLoanNumber(requestDto.getLoanNumber());
            collateral.setStatus("PLEDGED");
            collateral.setPledgeDate(LocalDate.now());
        }

        // Create valuation history
        CollateralValuation valuation = CollateralValuation.builder()
                .collateral(collateral)
                .valuationDate(requestDto.getValuationDate())
                .valuation(requestDto.getValuation())
                .valuationType("INITIAL")
                .build();

        collateral.getValuations().add(valuation);

        CollateralAsset saved = collateralRepository.save(collateral);
        log.info("Collateral registered: {}", collateralReference);

        return collateralMapper.toResponseDto(saved);
    }

    /**
     * Link collateral to loan
     */
    @Transactional
    public CollateralResponseDto linkCollateralToLoan(String collateralReference, String loanNumber) {
        log.info("Linking collateral {} to loan {}", collateralReference, loanNumber);

        CollateralAsset collateral = collateralRepository
                .findByCollateralReference(collateralReference)
                .orElseThrow(() -> new CollateralNotFoundException(
                        "Collateral not found: " + collateralReference
                ));

        // Validate collateral can be linked
        if (!"ACTIVE".equals(collateral.getStatus())) {
            throw new CollateralValidationException(
                    "Collateral cannot be linked (status: " + collateral.getStatus() + ")"
            );
        }

        collateral.setLoanNumber(loanNumber);
        collateral.setStatus("PLEDGED");
        collateral.setPledgeDate(LocalDate.now());
        collateral.setUpdatedBy("SYSTEM");

        CollateralAsset updated = collateralRepository.save(collateral);
        log.info("Collateral {} linked to loan {}", collateralReference, loanNumber);

        return collateralMapper.toResponseDto(updated);
    }

    /**
     * Release collateral from loan
     */
    @Transactional
    public CollateralResponseDto releaseCollateral(String collateralReference) {
        log.info("Releasing collateral: {}", collateralReference);

        CollateralAsset collateral = collateralRepository
                .findByCollateralReference(collateralReference)
                .orElseThrow(() -> new CollateralNotFoundException(
                        "Collateral not found: " + collateralReference
                ));

        // Validate collateral can be released
        if (!"PLEDGED".equals(collateral.getStatus())) {
            throw new CollateralValidationException(
                    "Collateral cannot be released (status: " + collateral.getStatus() + ")"
            );
        }

        collateral.setStatus("RELEASED");
        collateral.setReleaseDate(LocalDate.now());
        collateral.setLoanNumber(null);
        collateral.setUpdatedBy("SYSTEM");

        CollateralAsset updated = collateralRepository.save(collateral);
        log.info("Collateral released: {}", collateralReference);

        return collateralMapper.toResponseDto(updated);
    }

    /**
     * Validate collateral for loan
     *
     * WHY: This is the core integration method for Loan Service.
     * It validates that collateral is sufficient for the loan.
     */
    public CollateralValidationResponse validateCollateral(
            String customerNumber, String loanNumber, BigDecimal loanAmount) {

        log.info("Validating collateral for customer: {}, loan: {}",
                customerNumber, loanNumber);

        // Find all collateral for customer
        List<CollateralAsset> collaterals = collateralRepository
                .findByCustomerNumberAndStatus(customerNumber, "ACTIVE");

        if (collaterals.isEmpty()) {
            return CollateralValidationResponse.builder()
                    .valid(false)
                    .message("No active collateral found for customer")
                    .build();
        }

        // Calculate total collateral value
        BigDecimal totalValue = collaterals.stream()
                .map(CollateralAsset::getValuation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate Loan-to-Value ratio
        BigDecimal ltvRatio = loanAmount.divide(totalValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Check LTV ratio (typically 80% for real estate, 60% for other assets)
        // For simplicity, we use 80%
        boolean isValid = ltvRatio.compareTo(BigDecimal.valueOf(80)) <= 0;

        return CollateralValidationResponse.builder()
                .valid(isValid)
                .message(isValid ? "Collateral sufficient" : "Insufficient collateral")
                .totalValue(totalValue)
                .ltvRatio(ltvRatio)
                .collateralCount(collaterals.size())
                .build();
    }

    /**
     * Get collateral by reference
     */
    public CollateralResponseDto getByReference(String collateralReference) {
        CollateralAsset collateral = collateralRepository
                .findByCollateralReference(collateralReference)
                .orElseThrow(() -> new CollateralNotFoundException(
                        "Collateral not found: " + collateralReference
                ));
        return collateralMapper.toResponseDto(collateral);
    }

    /**
     * Get collateral by customer
     */
    public List<CollateralResponseDto> getByCustomer(String customerNumber) {
        List<CollateralAsset> collaterals = collateralRepository
                .findByCustomerNumber(customerNumber);
        return collateralMapper.toResponseDtoList(collaterals);
    }

    /**
     * Get collateral by loan
     */
    public List<CollateralResponseDto> getByLoan(String loanNumber) {
        List<CollateralAsset> collaterals = collateralRepository
                .findByLoanNumber(loanNumber);
        return collateralMapper.toResponseDtoList(collaterals);
    }

    /**
     * Link all active collateral to a loan by loan number
     *
     * WHY: Called by Loan Service after loan creation.
     * In a real system, this would look up the customer from the loan
     * and link all their ACTIVE collateral to the new loan.
     */
    @Transactional
    public void linkCollateralToLoan(String loanNumber) {
        log.warn("linkCollateralToLoan by loanNumber requires customer lookup in a real implementation");
        // In production, we would:
        // 1. Fetch loan details to get customerNumber
        // 2. Find all ACTIVE collateral for that customer
        // 3. Link each one to the loan (status = PLEDGED)
    }

    /**
     * Link all active collateral to a loan
     */
    @Transactional
    public void linkAllCollateralToLoan(String loanNumber) {
        log.info("Linking all active collateral to loan: {}", loanNumber);

        List<CollateralAsset> activeCollaterals = collateralRepository
            .findByStatus("ACTIVE");

        for (CollateralAsset collateral : activeCollaterals) {
            collateral.setLoanNumber(loanNumber);
            collateral.setStatus("PLEDGED");
            collateral.setPledgeDate(LocalDate.now());
            collateral.setUpdatedBy("SYSTEM");
        }

        collateralRepository.saveAll(activeCollaterals);
        log.info("Linked {} collateral assets to loan: {}",
            activeCollaterals.size(), loanNumber);
    }

    /**
     * Generate collateral reference
     */
    private String generateCollateralReference() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", (long) (Math.random() * 10000));
        String reference = "COL-" + date + "-" + sequence;

        // Ensure uniqueness
        while (collateralRepository.existsByCollateralReference(reference)) {
            sequence = String.format("%04d", (long) (Math.random() * 10000));
            reference = "COL-" + date + "-" + sequence;
        }

        return reference;
    }
}