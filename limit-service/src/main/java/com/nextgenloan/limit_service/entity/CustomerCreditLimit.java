// Purpose: Customer credit limit entity
// File: limit-service/src/main/java/com/nextgenloan/limit/entity/CustomerCreditLimit.java

package com.nextgenloan.limit_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_credit_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreditLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_number", length = 20, nullable = false, unique = true)
    private String customerNumber;

    @Column(name = "total_limit", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalLimit;

    @Column(name = "used_limit", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal usedLimit = BigDecimal.ZERO;

    @Column(name = "available_limit", precision = 19, scale = 2, nullable = false)
    private BigDecimal availableLimit;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "credit_rating", length = 20)
    private String creditRating;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

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

    @OneToMany(mappedBy = "customerLimit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductCreditLimit> productLimits = new ArrayList<>();
}