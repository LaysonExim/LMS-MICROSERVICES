// Purpose: Circuit breaker event listener
// File: api-gateway/src/main/java/com/nextgenloan/gateway/listener/CircuitBreakerEventListener.java
// Dependencies: Resilience4j

package com.nextgenloan.API.Gateway.listener;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Circuit Breaker Event Listener
 *
 * WHY: In banking, we need to know when circuit breakers open.
 * This provides:
 * 1. Alerting - When a circuit opens, send an alert
 * 2. Monitoring - Track circuit breaker state changes
 * 3. Debugging - Understand failure patterns
 * 4. Compliance - Document service outages
 *
 * In a real bank, this would:
 * 1. Send alerts to operations teams
 * 2. Log to centralized logging system
 * 3. Trigger automated recovery procedures
 * 4. Update dashboards
 */
@Configuration
@Slf4j
public class CircuitBreakerEventListener {

    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                CircuitBreaker circuitBreaker = entryAddedEvent.getAddedEntry();
                log.info("CircuitBreaker added: {}", circuitBreaker.getName());

                // Add event consumer for state transitions
                circuitBreaker.getEventPublisher()
                        .onStateTransition(event -> {
                            log.warn("CircuitBreaker {} state transition: {} → {}",
                                    circuitBreaker.getName(),
                                    event.getStateTransition().getFromState(),
                                    event.getStateTransition().getToState());

                            // If circuit opens, send alert
                            if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                                sendAlert(circuitBreaker.getName());
                            }
                        })
                        .onError(event -> {
                            log.error("CircuitBreaker {} recorded error: {}",
                                    circuitBreaker.getName(), event.getThrowable().getMessage());
                        })
                        .onSuccess(event -> {
                            log.debug("CircuitBreaker {} recorded success",
                                    circuitBreaker.getName());
                        })
                        .onCallNotPermitted(event -> {
                            log.warn("CircuitBreaker {} call not permitted (circuit open)",
                                    circuitBreaker.getName());
                        });
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                log.info("CircuitBreaker removed: {}", entryRemoveEvent.getRemovedEntry().getName());
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                log.info("CircuitBreaker replaced: {}", entryReplacedEvent.getOldEntry().getName());
            }
        };
    }

    private void sendAlert(String circuitBreakerName) {
        // In a real bank, this would:
        // 1. Send to PagerDuty/OpsGenie
        // 2. Send Slack message
        // 3. Send email to on-call engineer
        // 4. Trigger automated recovery
        log.error("🚨 ALERT: CircuitBreaker {} has OPENED! Service may be down!", circuitBreakerName);
    }
}