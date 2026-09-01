// Purpose: Lightweight DTO for customer lists
// File: customer-service/src/main/java/com/nextgenloan/customer/dto/CustomerSummaryDto.java
// Dependencies: Lombok

package com.nextgenloan.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer Summary DTO
 *
 * WHY: When returning a list of customers, we don't need all fields.
 * This is important for performance and network efficiency.
 *
 * For example, if we have 10,000 customers, sending all details
 * would be slow and wasteful. We send summary information for
 * the list, and clients can request detailed data for specific customers.
 *
 * REAL BANK EXAMPLE: In a banking API, the customer list endpoint
 * returns only essential data (name, customer number, status).
 * The full details are available via a separate endpoint.
 * This is both performant and secure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDto {

    private String customerNumber;

    private String firstName;

    private String lastName;

    private String email;

    private String status;
}