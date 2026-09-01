package com.nextgenloan.collateral_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CollateralServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollateralServiceApplication.class, args);
		System.out.println("==========================================");
		System.out.println("Collateral Service is running on port 8084!");
		System.out.println("==========================================");
	}
}