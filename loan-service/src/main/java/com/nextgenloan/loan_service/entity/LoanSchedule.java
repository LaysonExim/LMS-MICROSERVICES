// Purpose: JPA entity for loan schedules
// File: loan-service/src/main/java/com/nextgenloan/loan/entity/LoanSchedule.java

package com.nextgenloan.loan_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanApplication loan;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "installment_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal installmentAmount;

    @Column(name = "principal_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal interestAmount;

    @Column(name = "balance_after_installment", precision = 19, scale = 2, nullable = false)
    private BigDecimal balanceAfterInstallment;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}