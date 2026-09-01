package com.nextgenloan.collateral_service.entity;

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
@Table(name = "collateral_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollateralAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collateral_reference", length = 30, nullable = false, unique = true)
    private String collateralReference;

    @Column(name = "customer_number", length = 20, nullable = false)
    private String customerNumber;

    @Column(name = "asset_type", length = 50, nullable = false)
    private String assetType;

    @Column(name = "asset_name", length = 255, nullable = false)
    private String assetName;

    @Column(name = "asset_description", columnDefinition = "TEXT")
    private String assetDescription;

    @Column(name = "valuation", precision = 19, scale = 2, nullable = false)
    private BigDecimal valuation;

    @Column(name = "valuation_date", nullable = false)
    private LocalDate valuationDate;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "loan_number", length = 30)
    private String loanNumber;

    @Column(name = "pledge_date")
    private LocalDate pledgeDate;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "legal_status", length = 20)
    @Builder.Default
    private String legalStatus = "PENDING";

    @Column(name = "insurance_status", length = 20)
    @Builder.Default
    private String insuranceStatus = "PENDING";

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

    @OneToMany(mappedBy = "collateral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CollateralValuation> valuations = new ArrayList<>();
}