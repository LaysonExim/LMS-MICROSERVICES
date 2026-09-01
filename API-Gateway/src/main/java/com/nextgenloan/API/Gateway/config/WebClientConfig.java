// Purpose: WebClient configuration
// Dependencies: Spring WebFlux

package com.nextgenloan.API.Gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient Configuration
 *
 * WHY: WebClient is the reactive HTTP client for service calls.
 *
 * @LoadBalanced - Integrates with service discovery.
 * This means the URL "lb://customer-service" will be resolved
 * through Eureka.
 *
 * In a real bank, WebClient would also:
 * 1. Use TLS/SSL for security
 * 2. Have connection pooling
 * 3. Handle authentication
 * 4. Provide metrics
 * 5. Support load balancing
 */
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}