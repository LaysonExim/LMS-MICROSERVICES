// Purpose: Type-safe configuration properties for Customer Service
// File: customer-service/src/main/java/com/nextgenloan/customer/config/CustomerProperties.java
// Dependencies: Spring Boot Configuration Properties

package com.nextgenloan.customer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Customer Configuration Properties
 *
 * WHY: Type-safe access to configuration values.
 * Instead of using @Value annotations everywhere,
 * we encapsulate all configuration in a single class.
 *
 * This provides:
 * 1. Type safety - compilation errors instead of runtime errors
 * 2. Centralized config - all config in one place
 * 3. Auto-completion - IDE support for config properties
 * 4. Documentation - self-documenting configuration
 *
 * In a real bank, we'd use this pattern for all configuration.
 * It makes configuration changes much safer.
 *
 * @ConfigurationProperties - Binds configuration to this class
 * @RefreshScope - Allows configuration refresh without restart
 */
@Component
@Data
@ConfigurationProperties(prefix = "customer")
public class CustomerProperties {

    /**
     * Verification configuration
     */
    private VerificationConfig verification = new VerificationConfig();

    /**
     * Notification configuration
     */
    private NotificationConfig notifications = new NotificationConfig();

    @Data
    public static class VerificationConfig {
        /**
         * Whether verification is enabled
         */
        private boolean enabled = true;

        /**
         * URL of the KYC service
         */
        private String kycServiceUrl = "https://kyc-service.bank.com";

        /**
         * Maximum retry attempts for verification
         */
        private int maxRetries = 3;

        /**
         * Timeout in seconds for verification
         */
        private int timeoutSeconds = 30;
    }

    @Data
    public static class NotificationConfig {
        /**
         * Whether notifications are enabled
         */
        private boolean enabled = true;

        /**
         * Email notification configuration
         */
        private EmailConfig email = new EmailConfig();
    }

    @Data
    public static class EmailConfig {
        /**
         * Whether welcome emails are enabled
         */
        private boolean welcomeEnabled = true;

        /**
         * Email template name
         */
        private String template = "welcome-email.html";
    }
}