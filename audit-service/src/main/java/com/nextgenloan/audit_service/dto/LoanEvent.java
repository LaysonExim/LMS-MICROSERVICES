package com.nextgenloan.audit_service.dto;

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
public class LoanEvent {

    private String eventType;
    private String loanNumber;
    private String customerNumber;
    private String loanType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String firstName;
    private String lastName;
    private String email;
    private String correlationId;
    private LocalDateTime eventTimestamp;
    private String eventSource;
    private String eventVersion;
    private String additionalData;
}
