// Purpose: DTO for transferring customer data between layers
// File: customer-service/src/main/java/com/nextgenloan/customer/dto/CustomerDto.java
// Dependencies: Lombok

package com.nextgenloan.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Customer DTO
 *
 * WHY: We use DTOs to decouple our API from our internal data model.
 * In a bank, this is critical:
 *
 * 1. SECURITY: We don't expose internal fields (like version, created_at)
 * 2. EVOLUTION: We can change our database schema without breaking API clients
 * 3. VALIDATION: We can validate incoming data without saving to database
 * 4. PERFORMANCE: We can only send the fields the client needs
 *
 * REAL BANK EXAMPLE: In one bank I worked at, we changed the 'customer' table
 * to split 'name' into 'first_name' and 'last_name'. With DTOs, the API
 * continued to accept 'name' and we mapped it internally. Our API clients
 * didn't need to change. Without DTOs, we would have broken all our clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;

    @NotBlank(message = "Customer number is required")
    @Size(max = 20, message = "Customer number must be at most 20 characters")
    private String customerNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String status;

    /**
     * WHY: We only expose the customer number and status to the client.
     * We don't expose internal audit fields or the version field.
     * This is security by design.
     */
}