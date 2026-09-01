package com.nextgenloan.API.Gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {   // <-- renamed from CircuitBreakerConfig

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public CircuitBreaker customerServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("customerService");
    }

    @Bean
    public CircuitBreaker loanServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("loanService");
    }
}