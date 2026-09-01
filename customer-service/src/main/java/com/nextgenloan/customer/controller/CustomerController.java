// Purpose: Complete Customer REST API with proper boundaries
// File: customer-service/src/main/java/com/nextgenloan/customer/controller/CustomerController.java
// Dependencies: CustomerService

package com.nextgenloan.customer.controller;

import com.nextgenloan.customer.dto.*;
import com.nextgenloan.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer REST Controller
 *
 * WHY: This controller defines the SERVICE BOUNDARY.
 * It exposes what the Customer Service is responsible for:
 * 1. Customer registration and management
 * 2. Customer lookup (by customer number, name, email)
 * 3. Address management
 * 4. Status management
 *
 * WHAT IT DOES NOT DO:
 * 1. It doesn't handle loans (Loan Service does that)
 * 2. It doesn't handle payments (Payment Service does that)
 * 3. It doesn't handle limit management (Limit Service does that)
 *
 * This is the SERVICE BOUNDARY - clear, focused, and well-defined.
 *
 * REST DESIGN PRINCIPLES FOLLOWED:
 * 1. Resource-oriented URLs (customers, addresses)
 * 2. Proper HTTP methods (GET, POST, PUT, PATCH, DELETE)
 * 3. Proper HTTP status codes (200, 201, 400, 404, 409, etc.)
 * 4. Versioned API (/api/v1/...)
 * 5. Pagination, filtering, sorting for lists
 * 6. Consistent naming and response formats
 * 7. Comprehensive documentation
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Complete API for managing customer data")
public class CustomerController {

    private final CustomerService customerService;

    // ==================== Customer CRUD ====================

    /**
     * Register a new customer
     *
     * WHY: Entry point for new customers to the banking system.
     * The service will automatically set the status to PENDING_VERIFICATION.
     *
     * SECURITY: Rate limiting, input validation, anti-fraud checks
     *
     * HTTP: POST /api/v1/customers
     * Status: 201 Created (success)
     *         400 Bad Request (validation errors)
     *         409 Conflict (customer already exists)
     */
    @PostMapping
    @Operation(
            summary = "Register a new customer",
            description = "Creates a new customer in the system. The customer status is automatically set to PENDING_VERIFICATION."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid customer data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Customer already exists (email or customer number conflict)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerResponseDto> registerCustomer(
            @Valid @RequestBody CustomerRequestDto requestDto) {
        CustomerResponseDto created = customerService.registerCustomer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get a customer by customer number
     *
     * WHY: Customer number is the primary business key.
     * This is the preferred way to look up customers.
     *
     * HTTP: GET /api/v1/customers/{customerNumber}
     * Status: 200 OK (found)
     *         404 Not Found (customer doesn't exist)
     */
    @GetMapping("/{customerNumber}")
    @Operation(
            summary = "Get customer by customer number",
            description = "Retrieves a customer by their unique customer number (business key)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer found",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerResponseDto> getCustomer(
            @Parameter(description = "Unique customer number", required = true, example = "CUST-000001")
            @PathVariable String customerNumber) {
        CustomerResponseDto customer = customerService.getCustomerByNumber(customerNumber);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get customer details for internal service communication
     *
     * WHY: Other services need more data than external clients.
     * This endpoint returns internal fields like ID and version.
     *
     * SECURITY: This endpoint should be secured with service-to-service
     * authentication (client credentials grant type).
     *
     * HTTP: GET /api/v1/customers/internal/{customerNumber}
     * Status: 200 OK (found)
     *         404 Not Found (customer doesn't exist)
     */
    @GetMapping("/internal/{customerNumber}")
    @Operation(
            summary = "Get customer for internal use",
            description = "Retrieves customer details including internal fields for service-to-service communication"
    )
    public ResponseEntity<CustomerInternalDto> getCustomerInternal(
            @Parameter(description = "Unique customer number", required = true, example = "CUST-000001")
            @PathVariable String customerNumber) {
        CustomerInternalDto customer = customerService.getCustomerInternal(customerNumber);
        return ResponseEntity.ok(customer);
    }

    /**
     * Update customer information
     *
     * WHY: Customer information changes over time.
     * This performs a full update (PUT) - all fields must be provided.
     * For partial updates, use PATCH.
     *
     * HTTP: PUT /api/v1/customers/{customerNumber}
     * Status: 200 OK (updated)
     *         400 Bad Request (validation errors)
     *         404 Not Found (customer doesn't exist)
     */
    @PutMapping("/{customerNumber}")
    @Operation(
            summary = "Update customer",
            description = "Full update of customer information. All fields must be provided."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid customer data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @Parameter(description = "Unique customer number", required = true, example = "CUST-000001")
            @PathVariable String customerNumber,
            @Valid @RequestBody CustomerRequestDto requestDto) {
        CustomerResponseDto updated = customerService.updateCustomer(customerNumber, requestDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deactivate customer (soft delete)
     *
     * WHY: We never hard delete customers in banking.
     * Deactivation sets status to INACTIVE and preserves data for audits.
     *
     * HTTP: DELETE /api/v1/customers/{customerNumber}
     * Status: 204 No Content (deactivated)
     *         404 Not Found (customer doesn't exist)
     */
    @DeleteMapping("/{customerNumber}")
    @Operation(
            summary = "Deactivate customer",
            description = "Soft delete - sets customer status to INACTIVE. Data is preserved for audits."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Void> deactivateCustomer(
            @Parameter(description = "Unique customer number", required = true, example = "CUST-000001")
            @PathVariable String customerNumber) {
        customerService.deactivateCustomer(customerNumber);
        return ResponseEntity.noContent().build();
    }

    // ==================== Status Management ====================

    /**
     * Update customer status
     *
     * WHY: Status changes are controlled operations.
     * Only authorized users can change customer status.
     *
      * HTTP: PATCH /api/v1/customers/{customerNumber}/status
      * Body: {"status": "ACTIVE"}       
     * Status: 200 OK (updated)
     *         400 Bad Request (invalid status)
     *         404 Not Found (customer doesn't exist)
     */
    @PatchMapping("/{customerNumber}/status")
    @Operation(
            summary = "Update customer status",
            description = "Updates a customer's status. Allowed statuses: ACTIVE, INACTIVE, BLOCKED, PENDING_VERIFICATION"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerResponseDto> updateStatus(
            @Parameter(description = "Unique customer number", required = true, example = "CUST-000001")
            @PathVariable String customerNumber,
            @Valid @RequestBody StatusUpdateRequest request) {
        CustomerResponseDto updated = customerService.updateCustomerStatus(customerNumber, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    // ==================== List and Search ====================

    /**
     * Get all customers with pagination, filtering, and sorting
     *
     * WHY: Efficient data retrieval for UI lists and reports.
     *
     * PARAMETERS:
     * - page: Page number (0-based, default: 0)
     * - size: Items per page (default: 20, max: 100)
     * - sort: Sort by field (e.g., lastName,asc)
     * - status: Filter by status (ACTIVE, INACTIVE, etc.)
     * - search: Search by name or email
     *
     * HTTP: GET /api/v1/customers?page=0&size=20&sort=lastName,asc&status=ACTIVE&search=John
     * Status: 200 OK
     */
    @GetMapping
    @Operation(
            summary = "List customers",
            description = "Retrieves a paginated list of customers with optional filtering and sorting."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Customers retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    public ResponseEntity<Page<CustomerSummaryDto>> listCustomers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort by (e.g., lastName,asc)", example = "lastName,asc")
            @RequestParam(defaultValue = "lastName,asc") String sort,

            @Parameter(description = "Filter by status", example = "ACTIVE")
            @RequestParam(required = false) String status,

            @Parameter(description = "Search by name or email", example = "John")
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<CustomerSummaryDto> customers = customerService.listCustomers(
                pageable, status, search);
        return ResponseEntity.ok(customers);
    }

    /**
     * Advanced search customers with complex criteria
     *
     * WHY: Sometimes clients need complex search.
     * This allows JSON-based search criteria.
     *
     * HTTP: POST /api/v1/customers/search
     * Body: SearchCriteria JSON
     * Status: 200 OK
     */
    @PostMapping("/search")
    @Operation(
            summary = "Advanced search customers",
            description = "Performs advanced search with complex criteria."
    )
    public ResponseEntity<Page<CustomerSummaryDto>> searchCustomers(
            @RequestBody SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                parseSort(searchRequest.getSort())
        );
        Page<CustomerSummaryDto> customers = customerService.searchCustomers(
                pageable, searchRequest);
        return ResponseEntity.ok(customers);
    }

    // ==================== Address Management ====================

    /**
     * Get customer addresses
     *
     * WHY: Addresses are separate resources under a customer.
     * This allows independent management of addresses.
     *
     * HTTP: GET /api/v1/customers/{customerNumber}/addresses
     * Status: 200 OK
     *         404 Not Found (customer doesn't exist)
     */
    @GetMapping("/{customerNumber}/addresses")
    @Operation(
            summary = "Get customer addresses",
            description = "Retrieves all addresses for a customer."
    )
    public ResponseEntity<List<AddressDto>> getCustomerAddresses(
            @Parameter(description = "Unique customer number", required = true, example = "CUST-000001")
            @PathVariable String customerNumber) {
        List<AddressDto> addresses = customerService.getCustomerAddresses(customerNumber);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Add customer address
     *
     * WHY: Add a new address to an existing customer.
     *
     * HTTP: POST /api/v1/customers/{customerNumber}/addresses
     * Status: 201 Created
     *         404 Not Found (customer doesn't exist)
     */
    @PostMapping("/{customerNumber}/addresses")
    @Operation(
            summary = "Add customer address",
            description = "Adds a new address to an existing customer."
    )
    public ResponseEntity<AddressDto> addCustomerAddress(
            @Parameter(description = "Unique customer number", required = true, example = "CUST-000001")
            @PathVariable String customerNumber,
            @Valid @RequestBody AddressRequestDto addressDto) {
        AddressDto created = customerService.addAddress(customerNumber, addressDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update customer address
     *
     * WHY: Addresses can change over time.
     *
     * HTTP: PUT /api/v1/customers/{customerNumber}/addresses/{addressId}
     * Status: 200 OK
     *         404 Not Found (customer or address doesn't exist)
     */
    @PutMapping("/{customerNumber}/addresses/{addressId}")
    @Operation(
            summary = "Update customer address",
            description = "Updates an existing customer address."
    )
    public ResponseEntity<AddressDto> updateCustomerAddress(
            @PathVariable String customerNumber,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequestDto addressDto) {
        AddressDto updated = customerService.updateAddress(customerNumber, addressId, addressDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete customer address
     *
     * WHY: Remove an address that's no longer valid.
     *
     * HTTP: DELETE /api/v1/customers/{customerNumber}/addresses/{addressId}
     * Status: 204 No Content
     *         404 Not Found (customer or address doesn't exist)
     */
    @DeleteMapping("/{customerNumber}/addresses/{addressId}")
    @Operation(
            summary = "Delete customer address",
            description = "Removes a customer address."
    )
    public ResponseEntity<Void> deleteCustomerAddress(
            @PathVariable String customerNumber,
            @PathVariable Long addressId) {
        customerService.deleteAddress(customerNumber, addressId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Helper Methods ====================

    private Sort parseSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by("lastName").ascending();
        }
        String[] parts = sort.split(",");
        if (parts.length == 1) {
            return Sort.by(parts[0]).ascending();
        }
        String field = parts[0];
        String direction = parts[1];
        return direction.equalsIgnoreCase("desc")
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }
}