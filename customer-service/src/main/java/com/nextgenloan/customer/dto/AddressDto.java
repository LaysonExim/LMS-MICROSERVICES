package com.nextgenloan.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private Long id;
    private Long customerId;
    private String customerNumber;
    private String addressType;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isPrimary;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}