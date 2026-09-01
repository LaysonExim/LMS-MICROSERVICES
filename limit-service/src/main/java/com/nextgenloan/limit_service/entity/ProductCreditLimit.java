// Purpose: Product credit limit entity
// File: limit-service/src/main/java/com/nextgenloan/limit/entity/ProductCreditLimit.java

package com.nextgenloan.limit_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_credit_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreditLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_limit_id", nullable = false)
    private CustomerCreditLimit customerLimit;

    @Column(name = "product_type", length = 50, nullable = false)
    private String productType;

    @Column(name = "limit_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal limitAmount;

    @Column(name = "used_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal usedAmount = BigDecimal.ZERO;

    @Column(name = "available_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal availableAmount;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}