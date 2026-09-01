// Purpose: DTO for loan schedules
// File: loan-service/src/main/java/com/nextgenloan/loan/dto/LoanScheduleDto.java

package com.nextgenloan.loan_service.dto;

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
public class LoanScheduleDto {
    private Long id;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal installmentAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal balanceAfterInstallment;
    private String status;
    private LocalDate paidDate;
}