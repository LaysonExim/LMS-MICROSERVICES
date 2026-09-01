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
public class CollateralValidationRequest {
    private String customerNumber;
    private String loanNumber;
    private BigDecimal loanAmount;
}
