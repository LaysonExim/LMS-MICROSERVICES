// Purpose: Internal DTO for service-to-service communication
// File: customer-service/src/main/java/com/nextgenloan/customer/dto/CustomerInternalDto.java
// Dependencies: Lombok

package com.nextgenloan.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Customer Internal DTO
 *
 * WHY: When services communicate with each other, they need
 * more information than what's exposed to external clients.
 *
 * This DTO includes:
 * - Internal ID (for database references)
 * - Customer number (business key)
 * - All customer information
 * - Status (for business decisions)
 *
 * SECURITY: This is NOT exposed to external clients.
 * It's only used between services (internal communication).
 *
 * REAL BANK EXAMPLE: The Loan Service needs to know customer details
 * to process a loan application. It calls the Customer Service's
 * internal API (secured with service-to-service authentication)
 * and receives this DTO. The Customer Service trusts the Loan Service
 * (it's an internal service) but still validates the request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInternalDto {

    private Long id;                    // Internal database ID
    private String customerNumber;      // Business key
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer version;            // For optimistic locking

    // 
    // EXPLANATION: Why include internal fields here?
    // Internal services might need:
    // 1. The internal ID for creating foreign key relationships
    // 2. The version for optimistic locking in distributed transactions
    // 3. Audit timestamps for logging and troubleshooting
    // 
    // But these are only exposed to trusted internal services.
    // 
}