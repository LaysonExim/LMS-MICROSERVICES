// Purpose: Client for calling Customer Service with circuit breaker
// Dependencies: Resilience4j, WebClient


package com.nextgenloan.API.Gateway.service;

import com.nextgenloan.API.Gateway.dto.CustomerDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Customer Service Client
 *
 * WHY: This client encapsulates all calls to Customer Service.
 * It provides circuit breaker, retry, timeout, and bulkhead.
 *
 * In a real bank, this would be a critical component.
 * All service-to-service communication goes through clients like this.
 *
 * ANNOTATION EXPLANATION:
 *
 * @CircuitBreaker - Opens the circuit when failures exceed threshold
 * @Retry - Retries transient failures
 * @Bulkhead - Limits concurrent calls
 *
 * FALLBACK: When the circuit is open or the service fails,
 * the fallback method returns a degraded response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceClient {

    private final WebClient webClient;

    private static final String CUSTOMER_SERVICE = "customer-service";

    /**
     * Get customer by ID
     *
     * WHY: This is a critical operation.
     * When it fails, we need to handle it gracefully.
     *
     * RESILIENCE:
     * 1. Circuit Breaker - Opens when failure rate > 50%
     * 2. Retry - Retries up to 3 times on transient failures
     * 3. Timeout - Fails after 5 seconds
     * 4. Bulkhead - Max 10 concurrent calls
     *
     * FALLBACK: Returns a default customer with status "UNKNOWN"
     * This allows the system to continue functioning even when
     * Customer Service is unavailable.
     */
    @CircuitBreaker(name = CUSTOMER_SERVICE, fallbackMethod = "getCustomerFallback")
    @Retry(name = CUSTOMER_SERVICE)
    @Bulkhead(name = CUSTOMER_SERVICE)
    public Mono<CustomerDto> getCustomer(String customerNumber) {
        log.info("Calling Customer Service for customer: {}", customerNumber);

        return webClient.get()
                .uri("lb://customer-service/api/v1/customers/{customerNumber}", customerNumber)
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .doOnSuccess(result -> log.info("Successfully retrieved customer: {}", customerNumber))
                .doOnError(error -> log.error("Error retrieving customer: {}", error.getMessage()))
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Fallback method for getCustomer
     *
     * WHY: When Customer Service is unavailable, we return a default.
     * This allows the system to continue functioning.
     *
     * In a real bank, we might:
     * 1. Return cached data
     * 2. Return default values
     * 3. Return partial data
     * 4. Return a generic error
     *
     * The fallback must match the signature of the method it's falling back to.
     */
    public Mono<CustomerDto> getCustomerFallback(String customerNumber, Exception ex) {
        log.warn("Fallback for customer: {}, Error: {}", customerNumber, ex.getMessage());

        // Create a default/fallback customer
        CustomerDto fallback = CustomerDto.builder()
                .customerNumber(customerNumber)
                .firstName("Unknown")
                .lastName("Customer")
                .email("unknown@bank.com")
                .status("UNKNOWN")
                .build();

        return Mono.just(fallback);
    }

    /**
     * Get all customers
     *
     * WHY: List operation with resilience.
     *
     * FALLBACK: Returns an empty list.
     * This allows the UI to show "No customers available" instead of an error.
     */
    @CircuitBreaker(name = CUSTOMER_SERVICE, fallbackMethod = "getAllCustomersFallback")
    @Retry(name = CUSTOMER_SERVICE)
    @Bulkhead(name = CUSTOMER_SERVICE)
    public Mono<List<CustomerDto>> getAllCustomers() {
        log.info("Calling Customer Service for all customers");

        return webClient.get()
                .uri("lb://customer-service/api/v1/customers?size=100")
                .retrieve()
                .bodyToMono(Map.class)
                .map(page -> {
                    List<Map<String, Object>> content = (List<Map<String, Object>>) page.get("content");
                    if (content == null) {
                        return List.<CustomerDto>of();
                    }
                    return content.stream()
                            .map(item -> CustomerDto.builder()
                                    .customerNumber((String) item.get("customerNumber"))
                                    .firstName((String) item.get("firstName"))
                                    .lastName((String) item.get("lastName"))
                                    .email((String) item.get("email"))
                                    .status((String) item.get("status"))
                                    .build())
                            .collect(Collectors.toList());
                })
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Fallback for getAllCustomers
     */
    public Mono<List<CustomerDto>> getAllCustomersFallback(Exception ex) {
        log.warn("Fallback for getAllCustomers, Error: {}", ex.getMessage());
        return Mono.just(List.of());
    }

    /**
     * Create customer
     *
     * WHY: Write operation with resilience.
     *
     * In a bank, writes are more critical than reads.
     * We might use a different circuit breaker configuration for writes.
     */
    @CircuitBreaker(name = CUSTOMER_SERVICE, fallbackMethod = "createCustomerFallback")
    @Retry(name = CUSTOMER_SERVICE)
    @Bulkhead(name = CUSTOMER_SERVICE)
    public Mono<CustomerDto> createCustomer(CustomerDto customerDto) {
        log.info("Creating customer: {}", customerDto.getCustomerNumber());

        return webClient.post()
                .uri("lb://customer-service/api/v1/customers")
                .bodyValue(customerDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalArgumentException("Client error: " + body))))
                .bodyToMono(CustomerDto.class)
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Fallback for createCustomer
     */
    public Mono<CustomerDto> createCustomerFallback(CustomerDto customerDto, Exception ex) {
        log.warn("Fallback for createCustomer, Error: {}", ex.getMessage());

        if (ex instanceof IllegalArgumentException) {
            return Mono.error(ex);
        }

        customerDto.setStatus("PENDING_CREATION");
        return Mono.just(customerDto);
    }
}