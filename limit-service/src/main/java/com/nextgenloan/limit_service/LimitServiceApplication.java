// Purpose: Credit Limit Service main application
// File: limit-service/src/main/java/com/nextgenloan/limit/LimitServiceApplication.java

package com.nextgenloan.limit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LimitServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimitServiceApplication.class, args);
		System.out.println("==========================================");
		System.out.println("Credit Limit Service is running on port 8083!");
		System.out.println("==========================================");
	}
}