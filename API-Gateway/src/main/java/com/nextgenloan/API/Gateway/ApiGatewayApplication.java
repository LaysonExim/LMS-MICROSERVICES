// Purpose: API Gateway application
// File: api-gateway/src/main/java/com/nextgenloan/gateway/ApiGatewayApplication.java
// Dependencies: Spring Cloud, Gateway

package com.nextgenloan.API.Gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 *
 * WHY: This is the single entry point for all clients.
 *
 * WHAT IT DOES:
 * 1. Routes requests to appropriate services
 * 2. Handles authentication and authorization
 * 3. Rate limiting
 * 4. Request/Response logging
 * 5. Security headers
 * 6. Circuit breaking
 * 7. Service discovery
 *
 * In a real bank, the API Gateway is the most critical
 * piece of infrastructure. It handles ALL incoming traffic.
 *
 * @EnableDiscoveryClient - Registers with Eureka
 *
 * KEY CONCEPTS:
 * 1. Routes - Define where requests go
 * 2. Predicates - Conditions for routing
 * 3. Filters - Transform requests/responses
 * 4. Global Filters - Apply to all requests
 *
 * ROUTING EXAMPLES:
 * /api/v1/customers/** → customer-service
 * /api/v1/loans/** → loan-service
 * /api/v1/payments/** → payment-service
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

 	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
		System.out.println("==========================================");
		System.out.println("API Gateway is running on port 8080!");
		System.out.println("Routes:");
		System.out.println("  /api/v1/customers/**  -> customer-service");
		System.out.println("  /api/v1/loans/**      -> loan-service");
		System.out.println("  /api/v1/credit-limits/** -> limit-service");
		System.out.println("  /api/v1/collateral/** -> collateral-service");
		System.out.println("  /api/v1/reporting/**  -> reporting-service");
		System.out.println("  /api/v1/audit/**      -> audit-service");
		System.out.println("  /actuator/**          -> local gateway");
		System.out.println("==========================================");
	}
}