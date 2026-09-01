// Purpose: Repository interface for Customer entity
// File: customer-service/src/main/java/com/nextgenloan/customer/repository/CustomerRepository.java
// Dependencies: Spring Data JPA

package com.nextgenloan.customer.repository;

import com.nextgenloan.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Customer Repository
 *
 * WHY: In banking, we need a clean separation between data access
 * and business logic. The repository pattern provides this.
 *
 * WHAT: Spring Data JPA automatically implements all CRUD operations.
 *        We also add custom query methods.
 *
 * HOW IT'S USED: The Service layer calls the repository. This makes
 *                it easy to test business logic without hitting the
 *                real database.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * WHY: Find a customer by their business key.
     * This is the primary way other services will reference a customer.
     */
    Optional<Customer> findByCustomerNumber(String customerNumber);

    /**
     * WHY: Check if a customer exists by email.
     * Used for validation - we don't want duplicate emails.
     */
    boolean existsByEmail(String email);

    /**
     * WHY: Check if a customer exists by customer number.
     * Used to ensure uniqueness of business keys.
     */
    boolean existsByCustomerNumber(String customerNumber);

    /**
     * WHY: Search customers by name.
     * In a bank, we need to search customers by name for
     * customer service operations and fraud investigations.
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> searchByName(@Param("name") String name);

    /**
     * WHY: Find all active customers.
     * Used for batch operations, like sending marketing emails
     * or running reports.
     */
    List<Customer> findByStatus(String status);
}