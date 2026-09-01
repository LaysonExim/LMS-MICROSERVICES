package com.nextgenloan.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Status Update Request DTO
 *
 * Used for PATCH /api/v1/customers/{customerNumber}/status
 * Provides a clean request body for status updates,
 * validated by Swagger UI and the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED|PENDING_VERIFICATION",
            message = "Status must be one of: ACTIVE, INACTIVE, BLOCKED, PENDING_VERIFICATION")
    private String status;
}
