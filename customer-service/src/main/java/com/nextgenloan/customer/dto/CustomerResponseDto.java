// Purpose: Response DTO for customer data (API responses)
// File: customer-service/src/main/java/com/nextgenloan/customer/dto/CustomerResponseDto.java
// Dependencies: Lombok

package com.nextgenloan.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Customer Response DTO
 *
 * WHY: This DTO is used for OUTGOING responses (GET responses).
 * It defines what clients CAN SEE from the API.
 *
 * SECURITY: Notice what's NOT included:
 * - id (internal database ID, not exposed)
 * - version (internal field, not exposed)
 * - created_by, updated_by (internal audit, not exposed)
 *
 * WHAT IS INCLUDED:
 * - Customer number (business key)
 * - All public customer information
 * - Status (public information)
 * - Created at (for transparency, but not the user who created)
 *
 * REAL BANK EXAMPLE: In a banking API, we never expose
 * internal IDs to clients. This prevents enumeration attacks
 * and keeps internal architecture hidden. We use business keys
 * (customerNumber) instead of IDs in URLs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {

    private String customerNumber;      // Business key, NOT internal ID

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String status;              // ACTIVE, INACTIVE, etc.

    private OffsetDateTime createdAt;    // When the customer was created

    private OffsetDateTime updatedAt;    // When the customer was last updated

    // 
    // EXPLANATION: Why include created_at and updated_at?
    // Customers need to know when their profile was created
    // and when it was last updated. This is useful for:
    // 1. Customer awareness (security - "I didn't update this")
    // 2. Support teams (troubleshooting)
    // 3. Compliance (regulators might ask)
    // 
    // But we don't include WHO created/updated - that's internal.
    // 
}