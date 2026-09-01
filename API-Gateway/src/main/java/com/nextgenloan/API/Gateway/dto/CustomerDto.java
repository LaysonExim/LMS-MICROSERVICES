// Purpose: Customer DTO for gateway
// File: api-gateway/src/main/java/com/nextgenloan/gateway/dto/CustomerDto.java

package com.nextgenloan.API.Gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String status;
}