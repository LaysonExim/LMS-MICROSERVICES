// Purpose: Controller with resilience
// File: api-gateway/src/main/java/com/nextgenloan/gateway/controller/CustomerGatewayController.java
// Dependencies: CustomerServiceClient

package com.nextgenloan.API.Gateway.controller;

import com.nextgenloan.API.Gateway.dto.CustomerDto;
import com.nextgenloan.API.Gateway.service.CustomerServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Customer Gateway Controller
 * WHY: This controller demonstrates how to use the resilient client.
 * All calls to Customer Service go through the gateway and
 * benefit from circuit breaker protection.
 * In a real bank, this would be the main entry point for
 * all customer-related operations.
 */
@RestController
@RequestMapping("/api/v1/gateway/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerGatewayController {

    private final CustomerServiceClient customerServiceClient;

    /**
     * Get customer by customer number
     * WHY: This endpoint uses the resilient client.
     * If Customer Service is down, it returns a fallback.
     */
    @GetMapping("/{customerNumber}")
    @Operation(summary = "Get customer with resilience")
    public Mono<ResponseEntity<CustomerDto>> getCustomer(
            @PathVariable String customerNumber) {

        log.info("Gateway: Getting customer: {}", customerNumber);

        return customerServiceClient.getCustomer(customerNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get all customers
     * WHY: Returns fallback (empty list) if service is down.
     */
    @GetMapping
    @Operation(summary = "Get all customers with resilience")
    public Mono<ResponseEntity<List<CustomerDto>>> getAllCustomers() {
        log.info("Gateway: Getting all customers");

        return customerServiceClient.getAllCustomers()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * Create customer
     * WHY: If Customer Service is down, returns "PENDING_CREATION" status.
     * This allows the system to queue the creation for later.
     */
    @PostMapping
    @Operation(summary = "Create customer with resilience")
    public Mono<ResponseEntity<CustomerDto>> createCustomer(
            @RequestBody CustomerDto customerDto) {

        log.info("Gateway: Creating customer: {}", customerDto.getCustomerNumber());

        return customerServiceClient.createCustomer(customerDto)
                .map(created -> {
                    if ("PENDING_CREATION".equals(created.getStatus())) {
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(created);
                    }
                    return ResponseEntity.status(HttpStatus.CREATED).body(created);
                });
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationError(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> error = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "error", "Bad Request",
                "message", ex.getMessage()
        );
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    /**
     * Health check
     * WHY: This endpoint allows monitoring systems to check
     * if the gateway and its dependencies are healthy.
     * It calls Customer Service to verify connectivity.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check with dependency verification")
    public Mono<ResponseEntity<String>> healthCheck() {
        log.info("Gateway: Health check");

        return customerServiceClient.getAllCustomers()
                .map(customers -> {
                    log.info("Gateway: Customer Service is healthy, found {} customers", customers.size());
                    return ResponseEntity.ok("Gateway and Customer Service are healthy");
                })
                .onErrorResume(error -> {
                    log.error("Gateway: Customer Service is unhealthy: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body("Customer Service is unavailable"));
                });
    }
}