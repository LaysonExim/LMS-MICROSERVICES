// Purpose: Health check for event flow
// File: monitoring-service/src/main/java/com/nextgenloan/monitoring/health/EventHealthIndicator.java

package com.nextgenloan.monitoring_service.health;

import com.nextgenloan.monitoring_service.service.EventMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EventHealthIndicator implements HealthIndicator {

    private final EventMetricsService eventMetricsService;

    @Override
    public Health health() {
        Map<String, Object> stats = eventMetricsService.getEventStatistics();

        boolean healthy = (boolean) stats.get("healthy");

        if (healthy) {
            return Health.up()
                    .withDetail("status", "Event flow is healthy")
                    .withDetail("totalEvents", stats.get("totalEvents"))
                    .withDetail("timestamp", stats.get("timestamp"))
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", "Event flow is unhealthy")
                    .withDetail("totalEvents", stats.get("totalEvents"))
                    .withDetail("totalErrors", stats.get("totalErrors"))
                    .withDetail("timestamp", stats.get("timestamp"))
                    .build();
        }
    }
}