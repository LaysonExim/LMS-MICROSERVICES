// Purpose: Limit reservation entity
// File: limit-service/src/main/java/com/nextgenloan/limit/entity/LimitReservation.java

package com.nextgenloan.limit_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "limit_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", length = 50, nullable = false, unique = true)
    private String reservationId;

    @Column(name = "customer_number", length = 20, nullable = false)
    private String customerNumber;

    @Column(name = "product_type", length = 50, nullable = false)
    private String productType;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "loan_number", length = 30)
    private String loanNumber;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "reservation_date")
    @Builder.Default
    private LocalDateTime reservationDate = LocalDateTime.now();

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "released_date")
    private LocalDateTime releasedDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}