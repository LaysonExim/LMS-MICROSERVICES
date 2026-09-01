// Purpose: Simple health check endpoint to verify service is running
// File: customer-service/src/main/java/com/nextgenloan/customer/controller/HealthController.java
// Dependencies: Spring Web

package com.nextgenloan.customer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Controller
 *
 * This is a simple controller that returns the service status.
 *
 * WHY: In a bank, we need to know if services are running.
 *      This endpoint is used by monitoring systems, load balancers,
 *      and deployment pipelines to check if the service is healthy.
 *      Without this, we can't build automated health checks.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    /**
     * Health check endpoint
     *
     * Returns basic service information
     *
     * In a real bank, this endpoint would also check:
     * - Database connectivity
     * - Disk space
     * - Memory usage
     * - Connection to dependent services
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "customer-service");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0-SNAPSHOT");
        return response;
    }
}