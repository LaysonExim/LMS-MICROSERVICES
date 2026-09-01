// Purpose: Response DTO for loan applications
// File: loan-service/src/main/java/com/nextgenloan/loan/dto/LoanResponseDto.java

package com.nextgenloan.loan_service.dto;

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
public class LoanResponseDto {

    private String loanNumber;
    private String customerNumber;
    private String loanType;
    private String loanPurpose;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String status;
    private LocalDateTime applicationDate;
    private LocalDateTime approvedDate;
    private LocalDateTime disbursementDate;
    private LocalDateTime closureDate;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Include schedules
    private List<LoanScheduleDto> schedules;
    private List<LoanRepaymentDto> repayments;
}