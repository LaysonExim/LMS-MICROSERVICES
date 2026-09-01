// Purpose: Customer DTO for service integration
// File: loan-service/src/main/java/com/nextgenloan/loan/dto/CustomerDto.java

package com.nextgenloan.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String status;
}