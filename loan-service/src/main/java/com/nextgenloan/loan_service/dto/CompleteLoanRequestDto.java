package com.nextgenloan.loan_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CompleteLoanRequestDto {

    @NotBlank(message = "Customer number is required")
    @Size(max = 20, message = "Customer number must be at most 20 characters")
    private String customerNumber;

    @NotBlank(message = "Loan type is required")
    @Size(max = 50, message = "Loan type must be at most 50 characters")
    private String loanType;

    @Size(max = 255, message = "Loan purpose must be at most 255 characters")
    private String loanPurpose;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate must be at least 0")
    private BigDecimal interestRate;

    @NotNull(message = "Term months is required")
    @DecimalMin(value = "1", message = "Term months must be at least 1")
    private Integer termMonths;

    @NotNull(message = "Collateral references are required")
    private List<String> collateralReferences;
}
