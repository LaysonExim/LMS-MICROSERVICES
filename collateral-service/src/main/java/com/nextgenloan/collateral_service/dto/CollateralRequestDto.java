package com.nextgenloan.collateral_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollateralRequestDto {

    @NotBlank(message = "Customer number is required")
    @Size(max = 20, message = "Customer number must be at most 20 characters")
    private String customerNumber;

    @NotBlank(message = "Asset type is required")
    @Size(max = 50, message = "Asset type must be at most 50 characters")
    private String assetType;

    @NotBlank(message = "Asset name is required")
    @Size(max = 255, message = "Asset name must be at most 255 characters")
    private String assetName;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String assetDescription;

    @NotNull(message = "Valuation is required")
    @DecimalMin(value = "0.01", message = "Valuation must be greater than 0")
    private BigDecimal valuation;

    @NotNull(message = "Valuation date is required")
    private LocalDate valuationDate;

    private String loanNumber;
}