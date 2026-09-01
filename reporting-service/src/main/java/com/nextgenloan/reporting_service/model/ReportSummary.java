package com.nextgenloan.reporting_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummary {
    private long totalLoans;
    private BigDecimal totalAmount;
    private Map<String, Long> loansByType;
    private Map<String, Long> loansByStatus;
    private LocalDateTime generatedAt;
}