package com.nextgenloan.loan_service.repository;

import com.nextgenloan.loan_service.entity.LoanApplication;
import com.nextgenloan.loan_service.entity.LoanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanScheduleRepository extends JpaRepository<LoanSchedule, Long> {

    List<LoanSchedule> findByLoanAndStatus(LoanApplication loan, String status);

    List<LoanSchedule> findByLoanAndStatusOrderByInstallmentNumberAsc(
            LoanApplication loan, String status);

    long countByLoanAndStatus(LoanApplication loan, String status);

    List<LoanSchedule> findByLoan(LoanApplication loan);
}