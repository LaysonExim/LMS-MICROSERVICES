package com.nextgenloan.loan_service.repository;

import com.nextgenloan.loan_service.entity.LoanApplication;
import com.nextgenloan.loan_service.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoan(LoanApplication loan);

    List<LoanRepayment> findByLoanOrderByRepaymentDateDesc(LoanApplication loan);
}