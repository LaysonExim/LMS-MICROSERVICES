package com.nextgenloan.API.Gateway;

import org.springframework.boot.SpringApplication;

public class TestApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.from(ApiGatewayApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
