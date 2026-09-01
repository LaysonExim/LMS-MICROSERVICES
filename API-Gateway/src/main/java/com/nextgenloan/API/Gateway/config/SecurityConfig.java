// Purpose: Security configuration for API Gateway
// File: api-gateway/src/main/java/com/nextgenloan/gateway/config/SecurityConfig.java
// Dependencies: Spring Security

package com.nextgenloan.API.Gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security Configuration for API Gateway
 *
 * WHY: The API Gateway is responsible for authentication.
 * All requests go through the gateway, so this is where
 * we enforce security.
 *
 * In a real bank, we'd implement:
 * 1. JWT validation
 * 2. OAuth2/OIDC
 * 3. Role-based authorization
 * 4. API key validation for partners
 *
 * For now, we allow all requests (development mode).
 * We'll implement proper security in Day 21.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll());

        return http.build();
    }
}