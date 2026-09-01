package com.nextgenloan.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollateralValidationResponse {
    private boolean valid;
    private String message;
    private BigDecimal totalValue;
    private BigDecimal ltvRatio;
    private Integer collateralCount;
}
