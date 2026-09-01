// Purpose: Client for Credit Limit Service
// File: loan-service/src/main/java/com/nextgenloan/loan/client/CreditLimitClient.java

package com.nextgenloan.loan_service.client;

import com.nextgenloan.loan_service.dto.CreditLimitCheckRequest;
import com.nextgenloan.loan_service.dto.CreditLimitCheckResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditLimitClient {

    private final WebClient webClient;
    private static final String LIMIT_SERVICE = "limit-service";

    /**
     * Check and reserve credit
     */
    @CircuitBreaker(name = LIMIT_SERVICE, fallbackMethod = "checkAndReserveCreditFallback")
    @Retry(name = LIMIT_SERVICE)
    @TimeLimiter(name = LIMIT_SERVICE)
    public Mono<CreditLimitCheckResponse> checkAndReserveCredit(CreditLimitCheckRequest request) {
        log.info("Calling Credit Limit Service for customer: {}", request.getCustomerNumber());

        return webClient.post()
                .uri("lb://limit-service/api/v1/credit-limits/check")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CreditLimitCheckResponse.class)
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Confirm reservation
     */
    @CircuitBreaker(name = LIMIT_SERVICE, fallbackMethod = "confirmReservationFallback")
    @Retry(name = LIMIT_SERVICE)
    @TimeLimiter(name = LIMIT_SERVICE)
    public Mono<Void> confirmReservation(String reservationId) {
        log.info("Confirming reservation: {}", reservationId);

        return webClient.post()
                .uri("lb://limit-service/api/v1/credit-limits/reservations/{reservationId}/confirm", reservationId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Release reservation
     */
    @CircuitBreaker(name = LIMIT_SERVICE, fallbackMethod = "releaseReservationFallback")
    @Retry(name = LIMIT_SERVICE)
    @TimeLimiter(name = LIMIT_SERVICE)
    public Mono<Void> releaseReservation(String reservationId) {
        log.info("Releasing reservation: {}", reservationId);

        return webClient.post()
                .uri("lb://limit-service/api/v1/credit-limits/reservations/{reservationId}/release", reservationId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5));
    }

    // Fallback methods
    public Mono<CreditLimitCheckResponse> checkAndReserveCreditFallback(
            CreditLimitCheckRequest request, Exception ex) {
        log.warn("Credit Limit Service fallback for customer: {}", request.getCustomerNumber());
        return Mono.just(CreditLimitCheckResponse.builder()
                .approved(false)
                .message("Credit Limit Service unavailable: " + ex.getMessage())
                .requestedAmount(request.getAmount())
                .build());
    }

    public Mono<Void> confirmReservationFallback(String reservationId, Exception ex) {
        log.warn("Credit Limit Service fallback for confirmation: {}", reservationId);
        return Mono.error(ex);
    }

    public Mono<Void> releaseReservationFallback(String reservationId, Exception ex) {
        log.warn("Credit Limit Service fallback for release: {}", reservationId);
        return Mono.error(ex);
    }
}