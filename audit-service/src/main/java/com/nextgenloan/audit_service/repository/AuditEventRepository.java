// Purpose: Advanced query repository
// File: audit-service/src/main/java/com/nextgenloan/audit/repository/AuditEventRepository.java

package com.nextgenloan.audit_service.repository;

import com.nextgenloan.audit_service.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    // Basic queries
    Page<AuditEvent> findByLoanNumber(String loanNumber, Pageable pageable);
    Page<AuditEvent> findByCustomerNumber(String customerNumber, Pageable pageable);
    Page<AuditEvent> findByEventType(String eventType, Pageable pageable);
    Page<AuditEvent> findByEventTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Advanced queries
    @Query("SELECT a FROM AuditEvent a WHERE a.loanNumber = :loanNumber AND a.eventType IN :eventTypes")
    Page<AuditEvent> findByLoanNumberAndEventTypes(
            @Param("loanNumber") String loanNumber,
            @Param("eventTypes") List<String> eventTypes,
            Pageable pageable
    );

    @Query("SELECT a FROM AuditEvent a WHERE a.customerNumber = :customerNumber AND a.eventTimestamp >= :fromDate")
    Page<AuditEvent> findByCustomerNumberAfterDate(
            @Param("customerNumber") String customerNumber,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable
    );

    @Query("SELECT a FROM AuditEvent a WHERE a.eventSource = :source AND a.eventTimestamp BETWEEN :from AND :to")
    Page<AuditEvent> findBySourceAndDateRange(
            @Param("source") String source,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    // Aggregation queries
    @Query("SELECT COUNT(a) FROM AuditEvent a WHERE a.eventType = :eventType AND a.eventTimestamp >= :fromDate")
    long countByEventTypeSince(
            @Param("eventType") String eventType,
            @Param("fromDate") LocalDateTime fromDate
    );

    @Query("SELECT a.eventType, COUNT(a) FROM AuditEvent a GROUP BY a.eventType")
    List<Object[]> countByEventType();

    @Query("SELECT a.eventSource, COUNT(a) FROM AuditEvent a GROUP BY a.eventSource")
    List<Object[]> countByEventSource();

    long countByEventTimestampBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(DISTINCT a.customerNumber) FROM AuditEvent a")
    long countDistinctCustomerNumbers();

    @Query("SELECT COUNT(DISTINCT a.loanNumber) FROM AuditEvent a")
    long countDistinctLoanNumbers();
}