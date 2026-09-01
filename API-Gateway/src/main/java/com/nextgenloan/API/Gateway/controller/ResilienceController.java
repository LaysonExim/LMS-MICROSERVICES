// Purpose: Resilience metrics endpoint
// File: api-gateway/src/main/java/com/nextgenloan/gateway/controller/ResilienceController.java

package com.nextgenloan.API.Gateway.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Resilience Metrics Controller
 * WHY: This endpoint shows the state of all resilience components.
 * In a real bank, this would be used by:
 * 1. Operations teams - to see service health
 * 2. Developers - to debug issues
 * 3. Monitoring systems - to collect metrics
 * 4. Incident response - to understand the current state
 */
@RestController
@RequestMapping("/api/v1/gateway/resilience")
@RequiredArgsConstructor
@Slf4j
public class ResilienceController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    /**
     * Get all circuit breaker states
     */
    @GetMapping("/circuitbreakers")
    public Map<String, Object> getCircuitBreakers() {
        Map<String, Object> result = new HashMap<>();

        for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
            Map<String, Object> state = new HashMap<>();
            state.put("state", circuitBreaker.getState().name());
            state.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
            state.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
            state.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
            state.put("numberOfBufferedCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls());
            result.put(circuitBreaker.getName(), state);
        }

        return result;
    }

    /**
     * Get all retry states
     */
    @GetMapping("/retries")
    public Map<String, Object> getRetries() {
        Map<String, Object> result = new HashMap<>();

        for (Retry retry : retryRegistry.getAllRetries()) {
            Map<String, Object> state = new HashMap<>();
            state.put("attempts", retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
            state.put("failedWithoutRetry", retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());

            result.put(retry.getName(), state);
        }

        return result;
    }
}