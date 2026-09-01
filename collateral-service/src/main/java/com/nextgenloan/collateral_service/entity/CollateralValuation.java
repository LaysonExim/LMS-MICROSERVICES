package com.nextgenloan.collateral_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "collateral_valuations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollateralValuation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collateral_id", nullable = false)
    private CollateralAsset collateral;

    @Column(name = "valuation_date", nullable = false)
    private LocalDate valuationDate;

    @Column(name = "valuation", precision = 19, scale = 2, nullable = false)
    private BigDecimal valuation;

    @Column(name = "valuation_type", length = 50)
    private String valuationType;

    @Column(name = "appraiser", length = 100)
    private String appraiser;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
