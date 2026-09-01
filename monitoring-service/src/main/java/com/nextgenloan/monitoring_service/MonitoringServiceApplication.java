package com.nextgenloan.monitoring_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitoringServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MonitoringServiceApplication.class, args);
		System.out.println("==========================================");
		System.out.println("Monitoring Service is running!");
		System.out.println("==========================================");
	}
}