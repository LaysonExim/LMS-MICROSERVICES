package com.nextgenloan.audit_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_source", nullable = false)
    private String eventSource;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "loan_number")
    private String loanNumber;

    @Column(name = "customer_number")
    private String customerNumber;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "amount")
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_data", columnDefinition = "NVARCHAR(MAX)")
    private Map<String, Object> additionalData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}