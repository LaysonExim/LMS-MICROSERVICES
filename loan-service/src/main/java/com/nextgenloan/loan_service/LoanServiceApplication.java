// Purpose: Loan Service main application
// File: loan-service/src/main/java/com/nextgenloan/loan/LoanServiceApplication.java

package com.nextgenloan.loan_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Loan Service Application
 *
 * WHY: This is the core service for loan management.
 *
 * RESPONSIBILITIES:
 * 1. Loan applications - Process new loan requests
 * 2. Loan booking - Record approved loans
 * 3. Loan schedule management - Manage repayment schedules
 * 4. Loan status management - Track loan lifecycle
 * 5. Interest calculation - Basic fixed interest
 * 6. Repayment tracking - Record repayments
 *
 * RELATIONSHIPS:
 * - References Customer Service (customer data)
 * - Will integrate with Limit Service (credit limits)
 * - Will integrate with Collateral Service (collateral)
 * - Will publish events to Notification/Audit services
 *
 * In a real bank, this service would process:
 * - Millions of loans
 * - Billions of dollars
 * - With complex business rules
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LoanServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanServiceApplication.class, args);
		System.out.println("==========================================");
		System.out.println("Loan Service is running on port 8082!");
		System.out.println("==========================================");
	}
}