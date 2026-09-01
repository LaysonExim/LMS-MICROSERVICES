package com.nextgenloan.reporting_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReportData {
    private String loanNumber;
    private String customerNumber;
    private String loanType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime disbursedAt;
}