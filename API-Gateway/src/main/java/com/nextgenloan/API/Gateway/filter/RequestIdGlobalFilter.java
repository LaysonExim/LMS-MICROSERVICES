// Purpose: Adds a unique X-Request-Id header to every incoming request
// File: api-gateway/src/main/java/com/nextgenloan/API/Gateway/filter/RequestIdGlobalFilter.java

package com.nextgenloan.API.Gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

/**
 * Request ID Filter
 *
 * WHY: Correlates a single request across gateway logs and
 * downstream service logs — critical for tracing issues
 * in a distributed banking system.
 *
 * Generates a fresh UUID per request and adds it as
 * X-Request-Id, since Spring Cloud Gateway's YAML-based
 * AddRequestHeader filter cannot generate dynamic values
 * like UUIDs at request time.
 */
@Configuration
public class RequestIdGlobalFilter {

    @Bean
    public GlobalFilter addRequestIdFilter() {   // <-- renamed, no longer matches class name
        return (exchange, chain) -> {
            String requestId = UUID.randomUUID().toString();

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Request-Id", requestId)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);
        };
    }
}