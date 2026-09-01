// Purpose: JPA entity for loan applications
// File: loan-service/src/main/java/com/nextgenloan/loan/entity/LoanApplication.java

package com.nextgenloan.loan_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_number", length = 30, nullable = false, unique = true)
    private String loanNumber;

    @Column(name = "customer_number", length = 20, nullable = false)
    private String customerNumber;

    @Column(name = "loan_type", length = 50, nullable = false)
    private String loanType;

    @Column(name = "loan_purpose", length = 255)
    private String loanPurpose;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "interest_rate", precision = 10, scale = 4, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "application_date")
    @Builder.Default
    private LocalDateTime applicationDate = LocalDateTime.now();

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;

    @Column(name = "closure_date")
    private LocalDateTime closureDate;

    // Denormalized customer fields
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    // Audit fields
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

    @Version
    @Column(name = "version")
    private Integer version;

    // Relationships
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LoanSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LoanRepayment> repayments = new ArrayList<>();
}