// Purpose: Monitoring endpoints
// File: monitoring-service/src/main/java/com/nextgenloan/monitoring/controller/MonitoringController.java

package com.nextgenloan.monitoring_service.controller;

import com.nextgenloan.monitoring_service.service.EventMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Monitoring", description = "Event flow monitoring")
public class MonitoringController {

    private final EventMetricsService eventMetricsService;

    /**
     * Get event statistics
     *
     * WHY: This provides visibility into the event flow.
     *
     * In a real bank, this would be used for:
     * 1. Dashboard monitoring
     * 2. Alerting (when errors spike)
     * 3. Capacity planning
     * 4. Performance analysis
     */
    @GetMapping("/events")
    @Operation(summary = "Get event statistics",
            description = "Retrieves statistics about the event flow")
    public ResponseEntity<Map<String, Object>> getEventStatistics() {
        log.info("Monitoring: Fetching event statistics");
        Map<String, Object> stats = eventMetricsService.getEventStatistics();
        return ResponseEntity.ok(stats);
    }
}