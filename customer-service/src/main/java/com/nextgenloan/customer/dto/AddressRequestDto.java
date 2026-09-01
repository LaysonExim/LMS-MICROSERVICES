package com.nextgenloan.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDto {
    @NotBlank(message = "Address type is required")
    @Pattern(regexp = "HOME|WORK|MAILING|OTHER", message = "Address type must be one of: HOME, WORK, MAILING, OTHER")
    private String addressType;  // HOME, WORK, MAILING, OTHER

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must be at most 255 characters")
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must be at most 20 characters")
    private String postalCode;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country = "US";

    private boolean isPrimary;
}