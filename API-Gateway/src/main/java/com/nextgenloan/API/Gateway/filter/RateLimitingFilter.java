// Purpose: Rate limiting filter
// Dependencies: Spring Cloud Gateway

package com.nextgenloan.API.Gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter
 *
 * WHY: In banking, rate limiting prevents:
 * 1. DDoS attacks - many requests from one source
 * 2. Resource exhaustion - services overwhelmed
 * 3. Cost issues - high API usage costs
 * 4. Fairness - ensuring fair usage across clients
 *
 * WHAT IT DOES:
 * - Limits requests per client per minute
 * - Returns 429 Too Many Requests when limit exceeded
 * - Uses client IP as identifier
 *
 * In a real bank, we'd use Redis for distributed rate limiting.
 * This implementation is for learning purposes only.
 */
@Configuration
@Slf4j
public class RateLimitingFilter {

    //
    // EXPLANATION: Rate Limiting Configuration
    // In a real bank:
    // - Partners: 1000 requests/minute
    // - Internal services: 5000 requests/minute
    // - Anonymous users: 60 requests/minute
    // - Authenticated users: 300 requests/minute
    //
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final Map<String, ClientRate> clientRates = new ConcurrentHashMap<>();

    @Bean
    public GlobalFilter rateLimitingGlobalFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String clientIp = getClientIp(request);

            // Check rate limit
            if (isRateLimited(clientIp)) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    private String getClientIp(ServerHttpRequest request) {
        // In a real bank, we'd check X-Forwarded-For headers
        // and handle proxies properly.
        return request.getRemoteAddress().getAddress().getHostAddress();
    }

    private boolean isRateLimited(String clientIp) {
        ClientRate rate = clientRates.computeIfAbsent(clientIp,
                k -> new ClientRate(0, Instant.now()));

        Instant now = Instant.now();
        if (now.isAfter(rate.getWindowStart().plusSeconds(60))) {
            // Reset window
            rate.setRequestCount(1);
            rate.setWindowStart(now);
            return false;
        }

        if (rate.getRequestCount() >= MAX_REQUESTS_PER_MINUTE) {
            return true;
        }

        rate.setRequestCount(rate.getRequestCount() + 1);
        return false;
    }

    private static class ClientRate {
        private int requestCount;
        private Instant windowStart;

        public ClientRate(int requestCount, Instant windowStart) {
            this.requestCount = requestCount;
            this.windowStart = windowStart;
        }

        public int getRequestCount() { return requestCount; }
        public void setRequestCount(int requestCount) { this.requestCount = requestCount; }
        public Instant getWindowStart() { return windowStart; }
        public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }
    }
}