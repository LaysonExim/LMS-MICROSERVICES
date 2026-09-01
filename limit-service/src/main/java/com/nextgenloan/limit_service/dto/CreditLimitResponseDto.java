// Purpose: Response DTO for credit limits
// File: limit-service/src/main/java/com/nextgenloan/limit/dto/CreditLimitResponseDto.java

package com.nextgenloan.limit_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLimitResponseDto {
    private String customerNumber;
    private BigDecimal totalLimit;
    private BigDecimal usedLimit;
    private BigDecimal availableLimit;
    private String currency;
    private Integer riskScore;
    private String creditRating;
    private List<ProductLimitDto> productLimits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductLimitDto {
        private String productType;
        private BigDecimal limitAmount;
        private BigDecimal usedAmount;
        private BigDecimal availableAmount;
    }
}