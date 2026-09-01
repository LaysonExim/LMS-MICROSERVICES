package com.nextgenloan.collateral_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollateralResponseDto {
    private String collateralReference;
    private String customerNumber;
    private String assetType;
    private String assetName;
    private String assetDescription;
    private BigDecimal valuation;
    private LocalDate valuationDate;
    private String currency;
    private String status;
    private String loanNumber;
    private LocalDate pledgeDate;
    private LocalDate releaseDate;
    private String legalStatus;
    private String insuranceStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer version;
    private List<ValuationDto> valuations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValuationDto {
        private Long id;
        private LocalDate valuationDate;
        private BigDecimal valuation;
        private String valuationType;
        private String appraiser;
        private String notes;
        private LocalDateTime createdAt;
    }
}
