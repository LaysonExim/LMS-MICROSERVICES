// Purpose: Add security headers to all responses
// Dependencies: Spring Cloud Gateway

package com.nextgenloan.API.Gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;

/**
 * Security Headers Filter
 *
 * WHY: Security headers protect against common attacks.
 * In banking, these are REQUIRED for security compliance.
 *
 * HEADERS ADDED:
 * 1. X-Content-Type-Options: nosniff - Prevents MIME type sniffing
 * 2. X-Frame-Options: DENY - Prevents clickjacking
 * 3. X-XSS-Protection: 1; mode=block - Prevents XSS attacks
 * 4. Strict-Transport-Security: max-age=31536000; includeSubDomains - Enforces HTTPS
 * 5. Content-Security-Policy: default-src 'self' - Controls content sources
 * 6. Referrer-Policy: strict-origin-when-cross-origin - Controls referrer information
 * 7. Permissions-Policy: geolocation=(), microphone=(), camera=() - Controls browser features
 *
 * In a real bank, these headers are critical for:
 * - PCI-DSS compliance
 * - ISO 27001 compliance
 * - Regulatory requirements
 */
@Configuration
public class SecurityHeadersFilter {

    @Bean
    public GlobalFilter securityHeadersGlobalFilter() {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();

            // 
            // EXPLANATION: Anti-MIME-Sniffing
            // Prevents browsers from interpreting files as something else.
            // This prevents a dangerous attack where an attacker
            // uploads a malicious file that gets executed.
            // 
            headers.add("X-Content-Type-Options", "nosniff");

            // 
            // EXPLANATION: Clickjacking Protection
            // Prevents the page from being loaded in an iframe.
            // This prevents clickjacking attacks where attackers
            // trick users into clicking hidden elements.
            // 
            headers.add("X-Frame-Options", "DENY");

            // 
            // EXPLANATION: XSS Protection
            // Enables the browser's built-in XSS filter.
            // Prevents Cross-Site Scripting attacks.
            // 
            headers.add("X-XSS-Protection", "1; mode=block");

            // 
            // EXPLANATION: HTTPS Enforcement
            // Tells browsers to only access the site via HTTPS.
            // This prevents man-in-the-middle attacks.
            // In production, this would be 31536000 (1 year).
            // 
            headers.add("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains");

            // 
            // EXPLANATION: Content Security Policy
            // Controls what content can be loaded.
            // This prevents XSS and data injection attacks.
            // 
            headers.add("Content-Security-Policy",
                    "default-src 'self'; script-src 'self'; style-src 'self';");

            // 
            // EXPLANATION: Referrer Policy
            // Controls what referrer information is sent.
            // This prevents leaking sensitive information.
            // 
            headers.add("Referrer-Policy",
                    "strict-origin-when-cross-origin");

            // 
            // EXPLANATION: Permissions Policy
            // Controls what browser features can be used.
            // This prevents abuse of browser features.
            // 
            headers.add("Permissions-Policy",
                    "geolocation=(), microphone=(), camera=()");

            return chain.filter(exchange);
        };
    }
}