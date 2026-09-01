// Purpose: Config Server application
// File: config-server/src/main/java/com/nextgenloan/config/ConfigServerApplication.java
// Dependencies: Spring Cloud, Config Server

package com.nextgenloan.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application
 *
 * WHY: This is the central configuration server for all services.
 *
 * WHAT IT DOES:
 * 1. Serves configuration from Git repository
 * 2. Supports environment-specific configuration
 * 3. Provides REST endpoints for fetching config
 * 4. Supports encrypted property values
 * 5. Enables dynamic config refresh
 *
 * @EnableConfigServer - This annotation makes this a Config Server
 *
 * CONFIGURATION ENDPOINTS:
 * - /{application}/{profile}[/{label}]
 * - /{application}-{profile}.yml
 * - /{application}-{profile}.properties
 * - /{label}/{application}-{profile}.yml
 *
 * In a real bank, Config Server would be:
 * - Highly available (multiple instances)
 * - Secured with authentication
 * - Integrated with Git (with access controls)
 * - Monitored with Prometheus
 * - Replicated across data centers
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
		System.out.println("==========================================");
		System.out.println("Config Server is running on port 8888!");
		System.out.println("Configuration endpoints: http://localhost:8888");
		System.out.println("==========================================");
	}
}