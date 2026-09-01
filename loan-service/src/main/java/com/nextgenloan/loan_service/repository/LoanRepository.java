// Purpose: Repository for loan applications
// File: loan-service/src/main/java/com/nextgenloan/loan/repository/LoanRepository.java

package com.nextgenloan.loan_service.repository;

import com.nextgenloan.loan_service.entity.LoanApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<LoanApplication, Long>,
        JpaSpecificationExecutor<LoanApplication> {

    Optional<LoanApplication> findByLoanNumber(String loanNumber);

    List<LoanApplication> findByCustomerNumber(String customerNumber);

    Page<LoanApplication> findByCustomerNumber(String customerNumber, Pageable pageable);

    List<LoanApplication> findByStatus(String status);

    Page<LoanApplication> findByStatus(String status, Pageable pageable);

    @Query("SELECT l FROM LoanApplication l WHERE l.customerNumber = :customerNumber AND l.status IN :statuses")
    List<LoanApplication> findByCustomerNumberAndStatusIn(
            @Param("customerNumber") String customerNumber,
            @Param("statuses") List<String> statuses
    );

    @Query("SELECT l FROM LoanApplication l WHERE l.status = :status AND l.createdAt < :threshold")
    List<LoanApplication> findByStatusAndCreatedAtBefore(
            @Param("status") String status,
            @Param("threshold") LocalDateTime threshold
    );

    boolean existsByLoanNumber(String loanNumber);

    long countByStatus(String status);
}