// Purpose: Enable Eureka Client
// File: customer-service/src/main/java/com/nextgenloan/customer/CustomerServiceApplication.java

package com.nextgenloan.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Customer Service Application
 *
 * WHY: This is now a microservice that registers with Eureka.
 *
 * @EnableDiscoveryClient - Makes this service register with Eureka
 *
 * WHAT HAPPENS:
 * 1. Service starts up
 * 2. It finds the Eureka Server (configured in application.yml)
 * 3. It registers itself (name, IP, port)
 * 4. It sends heartbeats every 30 seconds
 * 5. If the service stops, it unregisters
 *
 * In a real bank, this would be:
 * - Running on multiple instances (scaling)
 * - Each instance registers with Eureka
 * - Other services discover them via Eureka
 * - Load balancer distributes traffic
 */
@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka
public class CustomerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerServiceApplication.class, args);
		System.out.println("==========================================");
		System.out.println("Customer Service is running on port 8081!");
		System.out.println("Registered with Eureka: http://localhost:8761");
		System.out.println("==========================================");
	}
}