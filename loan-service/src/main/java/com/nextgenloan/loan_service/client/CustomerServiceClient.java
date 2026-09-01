// Purpose: Client for calling Customer Service
// File: loan-service/src/main/java/com/nextgenloan/loan/client/CustomerServiceClient.java
// Dependencies: WebClient, Resilience4j

package com.nextgenloan.loan_service.client;

import com.nextgenloan.loan_service.dto.CustomerDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Customer Service Client
 *
 * WHY: Loan Service needs customer data to process loans.
 * Instead of querying the Customer database directly,
 * we call the Customer Service API.
 *
 * This maintains the service boundary:
 * - Customer Service owns customer data
 * - Loan Service calls Customer Service for customer data
 * - No direct database access
 *
 * RESILIENCE: We use circuit breaker, retry, and timeout
 * to handle Customer Service failures gracefully.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceClient {

    private final WebClient webClient;

    private static final String CUSTOMER_SERVICE = "customer-service";

    /**
     * Get customer by customer number
     *
     * WHY: We need customer details to:
     * 1. Verify the customer exists
     * 2. Check customer status (active, blocked, etc.)
     * 3. Get customer name for loan documents
     * 4. Check customer creditworthiness
     *
     * In a real bank, we'd also check:
     * - Customer credit score
     * - Existing loans
     * - Payment history
     * - Fraud flags
     */
    @CircuitBreaker(name = CUSTOMER_SERVICE, fallbackMethod = "getCustomerFallback")
    @Retry(name = CUSTOMER_SERVICE)
    public Mono<CustomerDto> getCustomer(String customerNumber) {
        log.info("Calling Customer Service for customer: {}", customerNumber);

        return webClient.get()
                .uri("lb://customer-service/api/v1/customers/internal/{customerNumber}", customerNumber)
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .doOnSuccess(customer -> log.info("Retrieved customer: {}", customerNumber))
                .doOnError(error -> log.error("Error retrieving customer: {}", error.getMessage()))
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Fallback method for getCustomer
     *
     * WHY: When Customer Service is unavailable,
     * we return a default/fallback customer.
     *
     * This allows the Loan Service to continue
     * processing loans, even if customer data
     * is temporarily unavailable.
     *
     * In a real bank, this would be a cache
     * of customer data, not a default object.
     */
    public Mono<CustomerDto> getCustomerFallback(String customerNumber, Exception ex) {
        log.warn("Customer Service fallback for: {}, Error: {}", customerNumber, ex.getMessage());

        // Create a fallback customer
        return Mono.just(CustomerDto.builder()
                .customerNumber(customerNumber)
                .firstName("Unknown")
                .lastName("Customer")
                .email("unknown@bank.com")
                .status("UNKNOWN")
                .build());
    }

    /**
     * Validate customer exists and is active
     *
     * WHY: We need to ensure customers are valid
     * before processing loan applications.
     *
     * In a real bank, we'd check:
     * 1. Customer exists
     * 2. Customer is active (not blocked)
     * 3. Customer has no fraud flags
     * 4. Customer is eligible for loans
     *
     * This method returns true if the customer is valid,
     * false otherwise.
     */
    public Mono<Boolean> validateCustomer(String customerNumber) {
        return getCustomer(customerNumber)
                .map(customer -> {
                    if (customer == null) {
                        log.warn("Customer not found: {}", customerNumber);
                        return false;
                    }

                    // In a real bank, we'd check more conditions
                    boolean isValid = "ACTIVE".equals(customer.getStatus());

                    if (!isValid) {
                        log.warn("Customer {} is not active (status: {})",
                                customerNumber, customer.getStatus());
                    }

                    return isValid;
                })
                .onErrorReturn(false);
    }
}