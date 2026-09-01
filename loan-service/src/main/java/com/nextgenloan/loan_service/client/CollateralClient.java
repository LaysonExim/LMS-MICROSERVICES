package com.nextgenloan.loan_service.client;

import com.nextgenloan.loan_service.dto.CollateralValidationRequest;
import com.nextgenloan.loan_service.dto.CollateralValidationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollateralClient {

    private final WebClient webClient;
    private static final String COLLATERAL_SERVICE = "collateral-service";

    @CircuitBreaker(name = COLLATERAL_SERVICE, fallbackMethod = "validateCollateralFallback")
    @Retry(name = COLLATERAL_SERVICE)
    public Mono<CollateralValidationResponse> validateCollateral(
            CollateralValidationRequest request) {
        log.info("Calling Collateral Service for customer: {}",
                request.getCustomerNumber());

        return webClient.get()
                .uri("lb://collateral-service/api/v1/collateral/validate?customerNumber={customerNumber}&loanNumber={loanNumber}&loanAmount={loanAmount}",
                        request.getCustomerNumber(),
                        request.getLoanNumber() != null ? request.getLoanNumber() : "",
                        request.getLoanAmount())
                .retrieve()
                .bodyToMono(CollateralValidationResponse.class);
    }

    @CircuitBreaker(name = COLLATERAL_SERVICE, fallbackMethod = "linkCollateralToLoanFallback")
    @Retry(name = COLLATERAL_SERVICE)
    public Mono<Void> linkCollateralToLoan(String loanNumber) {
        log.info("Linking collateral to loan: {}", loanNumber);

        return webClient.post()
                .uri("lb://collateral-service/api/v1/collateral/link-all/{loanNumber}", loanNumber)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<CollateralValidationResponse> validateCollateralFallback(
            CollateralValidationRequest request, Exception ex) {
        log.warn("Collateral Service fallback for customer: {}",
                request.getCustomerNumber());
        return Mono.just(CollateralValidationResponse.builder()
                .valid(false)
                .message("Collateral Service unavailable: " + ex.getMessage())
                .build());
    }

    public Mono<Void> linkCollateralToLoanFallback(String loanNumber, Exception ex) {
        log.warn("Collateral Service fallback for link: {}", loanNumber);
        return Mono.empty();
    }
}
