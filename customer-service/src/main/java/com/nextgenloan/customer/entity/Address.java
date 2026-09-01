package com.nextgenloan.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customer_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "address_type", length = 20, nullable = false)
    private String addressType;

    @Column(length = 255, nullable = false)
    private String street;

    @Column(length = 100, nullable = false)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20, nullable = false)
    private String postalCode;

    @Column(length = 100)
    private String country;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}