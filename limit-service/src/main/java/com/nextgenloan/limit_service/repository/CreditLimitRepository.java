// Purpose: Repository interface for CustomerCreditLimit entity
// File: limit-service/src/main/java/com/nextgenloan/limit/repository/CreditLimitRepository.java
// Dependencies: Spring Data JPA

package com.nextgenloan.limit_service.repository;

import com.nextgenloan.limit_service.entity.CustomerCreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditLimitRepository extends JpaRepository<CustomerCreditLimit, Long> {

    Optional<CustomerCreditLimit> findByCustomerNumber(String customerNumber);
}
