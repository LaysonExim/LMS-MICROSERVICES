package com.nextgenloan.discovery_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Discovery Server Application
 *
 * WHY: This is the central registry for all microservices.
 * All services register here, and all services query here
 * to find each other.
 *
 * WHAT IT DOES:
 * 1. Maintains a registry of all running services
 * 2. Receives heartbeats from services (every 30 seconds)
 * 3. Removes services that stop sending heartbeats
 * 4. Provides service discovery API for clients
 * 5. Shows the dashboard (http://localhost:8761)
 *
 * In a real bank, Eureka Server would be:
 * - Highly available (multiple instances)
 * - Replicated across data centers
 * - Secured with authentication
 * - Monitored with Prometheus
 *
 * @EnableEurekaServer - This annotation makes this a Eureka Server
 * With this single annotation, we get a full service registry!
 *
 * TODO (Later): We'll add:
 * - Security (authentication for service registration)
 * - High availability (multiple Eureka servers)
 * - Health checks for services
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryServerApplication.class, args);
		System.out.println("==========================================");
		System.out.println("Discovery Server is running on port 8761!");
		System.out.println("Dashboard: http://localhost:8761");
		System.out.println("==========================================");
	}
}