package com.nextgenloan.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .description("Complete API for managing customer data in the NextGen Loan Management Platform")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("NextGen Loan Management Team")
                                .email("support@nextgenloan.com")
                                .url("https://www.nextgenloan.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.nextgenloan.com/legal")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Development Server"),
                        new Server().url("https://api-dev.bank.com/customer").description("Development Environment"),
                        new Server().url("https://api-uat.bank.com/customer").description("UAT Environment"),
                        new Server().url("https://api.bank.com/customer").description("Production Environment")
                ));
    }
}