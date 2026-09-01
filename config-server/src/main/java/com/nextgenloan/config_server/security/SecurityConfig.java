// Purpose: Security configuration for Config Server
// File: config-server/src/main/java/com/nextgenloan/config/security/SecurityConfig.java
// Dependencies: Spring Security

package com.nextgenloan.config_server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Config Server
 *
 * WHY: Config Server should be secured in production.
 * It contains sensitive configuration data.
 *
 * In a real bank, Config Server would have:
 * 1. HTTPS (encrypted communication)
 * 2. Authentication (services authenticate to fetch config)
 * 3. Authorization (read vs write access)
 * 4. Audit logging (who accessed what config)
 * 5. Encryption (secrets encrypted at rest)
 *
 * For development, we allow all requests for learning purposes.
 * We'll implement proper security later in the course.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}