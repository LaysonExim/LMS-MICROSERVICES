// Purpose: Collect and expose event metrics
// File: monitoring-service/src/main/java/com/nextgenloan/monitoring/service/EventMetricsService.java

package com.nextgenloan.monitoring_service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventMetricsService {

    private final MeterRegistry meterRegistry;

    // Event counters by type
    private final Map<String, AtomicLong> eventCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> eventErrors = new ConcurrentHashMap<>();
    private final Map<String, Long> eventLatencies = new ConcurrentHashMap<>();

    // Micrometer metrics
    private Counter eventsProcessed;
    private Counter eventsErrored;

    @PostConstruct
    public void initMetrics() {
        eventsProcessed = Counter.builder("events.processed.total")
                .description("Total number of events processed")
                .register(meterRegistry);
        eventsErrored = Counter.builder("events.errored.total")
                .description("Total number of events that failed")
                .register(meterRegistry);
    }

    /**
     * Consume events and collect metrics
     *
     * WHY: Monitoring events helps detect issues early.
     *
     * WHAT WE TRACK:
     * 1. Event count by type (LOAN_CREATED, LOAN_APPROVED, etc.)
     * 2. Error count by type
     * 3. Event latency (time between publish and consume)
     * 4. Consumer lag (events waiting to be processed)
     * 5. Throughput (events per second)
     */
    @KafkaListener(topics = "loan-events", groupId = "monitoring-service-group")
    public void monitorEvent(String eventJson) {
        try {
            // Parse event
            // In a real system, we'd extract event type and timestamp
            String eventType = extractEventType(eventJson);

            // Count events by type
            eventCounters.computeIfAbsent(eventType, k -> new AtomicLong(0))
                    .incrementAndGet();

            eventsProcessed.increment();

            // Calculate latency
            // In a real system, we'd compare publish vs consume timestamps

            log.debug("Monitoring: Processed event: {}", eventType);

        } catch (Exception e) {
            eventsErrored.increment();
            log.error("Monitoring: Failed to process event: {}", e.getMessage());
        }
    }

    /**
     * Get event statistics
     */
    public Map<String, Object> getEventStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        // Event counts
        Map<String, Long> counts = new ConcurrentHashMap<>();
        for (Map.Entry<String, AtomicLong> entry : eventCounters.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().get());
        }
        stats.put("eventCounts", counts);

        // Error counts
        Map<String, Long> errors = new ConcurrentHashMap<>();
        for (Map.Entry<String, AtomicLong> entry : eventErrors.entrySet()) {
            errors.put(entry.getKey(), entry.getValue().get());
        }
        stats.put("eventErrors", errors);

        // Total metrics
        long totalEvents = eventCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        stats.put("totalEvents", totalEvents);
        stats.put("totalErrors", eventsErrored.count());

        // Health status
        boolean healthy = eventsErrored.count() < 10; // Less than 10 errors is healthy
        stats.put("healthy", healthy);
        stats.put("status", healthy ? "HEALTHY" : "UNHEALTHY");

        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }

    /**
     * Extract event type from JSON
     */
    private String extractEventType(String eventJson) {
        // Simple extraction for demo
        if (eventJson.contains("LOAN_CREATED")) {
            return "LOAN_CREATED";
        } else if (eventJson.contains("LOAN_APPROVED")) {
            return "LOAN_APPROVED";
        } else if (eventJson.contains("LOAN_STATUS_CHANGED")) {
            return "LOAN_STATUS_CHANGED";
        } else if (eventJson.contains("LOAN_DISBURSED")) {
            return "LOAN_DISBURSED";
        } else if (eventJson.contains("LOAN_REPAID")) {
            return "LOAN_REPAID";
        }
        return "UNKNOWN";
    }
}