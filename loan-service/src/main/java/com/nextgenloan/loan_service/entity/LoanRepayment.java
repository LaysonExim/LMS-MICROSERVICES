// Purpose: JPA entity for loan repayments
// File: loan-service/src/main/java/com/nextgenloan/loan/entity/LoanRepayment.java

package com.nextgenloan.loan_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_repayments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanApplication loan;

    @Column(name = "repayment_reference", length = 50, nullable = false, unique = true)
    private String repaymentReference;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "principal_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal interestAmount;

    @Column(name = "repayment_date")
    @Builder.Default
    private LocalDateTime repaymentDate = LocalDateTime.now();

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}