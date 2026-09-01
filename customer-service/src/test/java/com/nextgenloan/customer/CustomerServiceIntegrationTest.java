// Purpose: Integration tests for Customer Service
// File: customer-service/src/test/java/com/nextgenloan/customer/CustomerServiceIntegrationTest.java
// Dependencies: Spring Boot Test, Testcontainers

package com.nextgenloan.customer;

import com.nextgenloan.customer.dto.CustomerRequestDto;
import com.nextgenloan.customer.dto.CustomerResponseDto;
import com.nextgenloan.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Customer Service Integration Tests
 *
 * WHY: In banking, integration tests are critical.
 * We test the entire flow: Controller → Service → Repository → Database
 * These tests use Testcontainers to spin up a real Microsoft SQL Server container.
 * This catches real database issues (not just in-memory H2).
 * In a real bank, integration tests would be run:
 * 1. During development (local)
 * 2. During CI/CD build (Jenkins/GitHub Actions)
 * 3. Before deployment (UAT environment)
 */
@SpringBootTest
@Testcontainers
public class CustomerServiceIntegrationTest {

	@Container
	@SuppressWarnings("resource")
	static MSSQLServerContainer<?> mssql = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
			.acceptLicense();

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mssql::getJdbcUrl);
		registry.add("spring.datasource.username", mssql::getUsername);
		registry.add("spring.datasource.password", mssql::getPassword);
		registry.add("spring.flyway.clean-disabled", () -> "false");
	}

	@Autowired
	private CustomerService customerService;

	@Test
	void shouldRegisterCustomer() {
		// Given
		CustomerRequestDto request = CustomerRequestDto.builder()
				.customerNumber("CUST-TEST-001")
				.firstName("Test")
				.lastName("User")
				.email("test@example.com")
				.phoneNumber("+1234567890")
				.dateOfBirth(LocalDate.of(1990, 1, 1))
				.build();

		// When
		CustomerResponseDto response = customerService.registerCustomer(request);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getCustomerNumber()).isEqualTo("CUST-TEST-001");
		assertThat(response.getFirstName()).isEqualTo("Test");
		assertThat(response.getLastName()).isEqualTo("User");
		assertThat(response.getEmail()).isEqualTo("test@example.com");
		assertThat(response.getStatus()).isEqualTo("PENDING_VERIFICATION");
	}

	@Test
	void shouldGetCustomerByNumber() {
		// Given
		CustomerRequestDto request = CustomerRequestDto.builder()
				.customerNumber("CUST-TEST-002")
				.firstName("Test2")
				.lastName("User2")
				.email("test2@example.com")
				.dateOfBirth(LocalDate.of(1990, 1, 1))
				.build();
		customerService.registerCustomer(request);

		// When
		CustomerResponseDto response = customerService.getCustomerByNumber("CUST-TEST-002");

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getCustomerNumber()).isEqualTo("CUST-TEST-002");
		assertThat(response.getFirstName()).isEqualTo("Test2");
		assertThat(response.getLastName()).isEqualTo("User2");
	}

	@Test
	void shouldUpdateCustomer() {
		// Given
		CustomerRequestDto request = CustomerRequestDto.builder()
				.customerNumber("CUST-TEST-003")
				.firstName("Test3")
				.lastName("User3")
				.email("test3@example.com")
				.dateOfBirth(LocalDate.of(1990, 1, 1))
				.build();
		customerService.registerCustomer(request);
		CustomerResponseDto existing = customerService.getCustomerByNumber("CUST-TEST-003");

		CustomerRequestDto updateRequest = CustomerRequestDto.builder()
				.customerNumber("CUST-TEST-003")
				.firstName("Updated")
				.lastName("Name")
				.email("updated@example.com")
				.phoneNumber("+9876543210")
				.dateOfBirth(LocalDate.of(1985, 1, 1))
				.build();

		// When
		CustomerResponseDto updated = customerService.updateCustomer(existing.getCustomerNumber(), updateRequest);

		// Then
		assertThat(updated.getFirstName()).isEqualTo("Updated");
		assertThat(updated.getLastName()).isEqualTo("Name");
		assertThat(updated.getEmail()).isEqualTo("updated@example.com");
	}

	@Test
	void shouldUpdateCustomerStatus() {
		// Given
		CustomerRequestDto request = CustomerRequestDto.builder()
				.customerNumber("CUST-TEST-004")
				.firstName("Test4")
				.lastName("User4")
				.email("test4@example.com")
				.dateOfBirth(LocalDate.of(1990, 1, 1))
				.build();
		CustomerResponseDto created = customerService.registerCustomer(request);

		// When
		CustomerResponseDto updated = customerService.updateCustomerStatus(created.getCustomerNumber(), "ACTIVE");

		// Then
		assertThat(updated.getStatus()).isEqualTo("ACTIVE");
	}
}