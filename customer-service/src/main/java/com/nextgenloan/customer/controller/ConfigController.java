// Purpose: Configuration management endpoints
// File: customer-service/src/main/java/com/nextgenloan/customer/controller/ConfigController.java
// Dependencies: Spring Cloud, Actuator

package com.nextgenloan.customer.controller;

import com.nextgenloan.customer.config.CustomerProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Controller
 *
 * WHY: This controller provides endpoints for viewing and refreshing
 * configuration. In a banking system, being able to refresh configuration
 * without restarting services is critical.
 *
 * REAL BANK EXAMPLE: During a production incident, we might need to
 * change a feature flag or adjust a timeout. With this endpoint,
 * we can make the change in Git and refresh all service instances
 * without restarting them. This reduces downtime and allows
 * faster response to incidents.
 */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configuration Management", description = "Endpoints for managing service configuration")
public class ConfigController {

    private final CustomerProperties customerProperties;
    private final ContextRefresher contextRefresher;

    /**
     * View current configuration
     *
     * WHY: This allows administrators to see the current
     * configuration values applied to the service.
     *
     * In a bank, this is useful for:
     * 1. Troubleshooting (what config is being used?)
     * 2. Audit (what config was applied when?)
     * 3. Compliance (are we using the correct config?)
     */
    @GetMapping
    @Operation(summary = "View current configuration")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();

        // Verification config
        config.put("verificationEnabled", customerProperties.getVerification().isEnabled());
        config.put("kycServiceUrl", customerProperties.getVerification().getKycServiceUrl());
        config.put("maxRetries", customerProperties.getVerification().getMaxRetries());
        config.put("timeoutSeconds", customerProperties.getVerification().getTimeoutSeconds());

        // Notification config
        config.put("notificationsEnabled", customerProperties.getNotifications().isEnabled());
        config.put("welcomeEmailsEnabled",
                customerProperties.getNotifications().getEmail().isWelcomeEnabled());
        config.put("emailTemplate",
                customerProperties.getNotifications().getEmail().getTemplate());

        return config;
    }

    /**
     * Refresh configuration
     *
     * WHY: This endpoint triggers a configuration refresh.
     * The service will reload its configuration from the Config Server.
     *
     * USE CASE:
     * 1. Change configuration in Git
     * 2. Commit and push the change
     * 3. Call this endpoint on all service instances
     * 4. Configuration is updated without restarting
     *
     * In a bank, this is used for:
     * 1. Feature flag changes
     * 2. Timeout adjustments
     * 3. URL changes for external services
     * 4. Rate limiting adjustments
     * 5. Emergency configuration changes
     *
     * SECURITY: This endpoint should be secured.
     * Only administrators should be able to refresh configuration.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh configuration",
            description = "Reloads configuration from the Config Server without restarting")
    public Map<String, Object> refreshConfig() {
        log.info("Refreshing configuration...");

        // Refresh the Spring context
        contextRefresher.refresh();

        // Return the updated configuration
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Configuration refreshed successfully");
        response.put("configuration", getConfig());
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        log.info("Configuration refreshed successfully");

        return response;
    }
}