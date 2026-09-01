// Purpose: Global filter for logging requests and responses
// Dependencies: Spring Cloud Gateway

package com.nextgenloan.API.Gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Logging Global Filter
 *
 * WHY: In banking, we need to log ALL incoming requests.
 * This is required for:
 * 1. Audit - who accessed what and when
 * 2. Troubleshooting - what happened before an error
 * 3. Compliance - regulatory requirements for logging
 * 4. Performance - tracking response times
 * 5. Security - detecting attacks
 *
 * WHAT IT DOES:
 * 1. Generates a correlation ID for each request
 * 2. Logs the request (method, path, headers)
 * 3. Logs the response (status, time taken)
 * 4. Adds correlation ID to response headers
 *
 * In a real bank, this would also:
 * - Log request bodies (with sensitive data redacted)
 * - Log user information
 * - Log to a centralized logging system
 * - Trigger alerts for suspicious requests
 */
@Configuration
@Slf4j
public class LoggingGlobalFilter {

    @Bean
    public GlobalFilter loggingFilter() {
        return (exchange, chain) -> {
            //
            // EXPLANATION: Correlation ID
            // This tracks a request across all services.
            // When a request passes through multiple services,
            // the correlation ID stays the same.
            //
            String correlationId = UUID.randomUUID().toString();

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Add correlation ID to request
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Correlation-Id", correlationId)
                    .build();

            // Add correlation ID to response
            response.getHeaders().add("X-Correlation-Id", correlationId);

            // Log the request
            logRequest(request, correlationId);

            // Measure execution time
            Instant start = Instant.now();

            // Continue processing
            return chain.filter(exchange.mutate()
                            .request(mutatedRequest)
                            .build())
                    .doOnSuccess(aVoid -> {
                        // Log the response
                        logResponse(response, correlationId, start);
                    })
                    .doOnError(error -> {
                        // Log errors
                        log.error("Request failed: {}, Error: {}", correlationId, error.getMessage());
                    });
        };
    }

    private void logRequest(ServerHttpRequest request, String correlationId) {
        log.info("=== Request {} ===", correlationId);
        log.info("Method: {}", request.getMethod());
        log.info("Path: {}", request.getURI().getPath());
        log.info("Headers: {}", request.getHeaders());
        log.info("Remote Address: {}", request.getRemoteAddress());
        // In a real bank, we'd also log:
        // - User information (from authentication)
        // - IP address (for security)
        // - Request body (with sensitive data redacted)
    }

    private void logResponse(ServerHttpResponse response, String correlationId, Instant start) {
        Duration duration = Duration.between(start, Instant.now());
        log.info("=== Response {} ===", correlationId);
        log.info("Status: {}", response.getStatusCode());
        log.info("Headers: {}", response.getHeaders());
        log.info("Duration: {}ms", duration.toMillis());
        log.info("=== End {} ===", correlationId);
    }
}