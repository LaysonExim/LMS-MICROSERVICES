// Purpose: Audit analytics
// File: audit-service/src/main/java/com/nextgenloan/audit/service/AuditAnalyticsService.java

package com.nextgenloan.audit_service.service;

import com.nextgenloan.audit_service.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditAnalyticsService {

    private final AuditEventRepository auditRepository;

    /**
     * Get audit statistics
     *
     * WHY: This provides insights into system activity.
     *
     * In a real bank, this would be used for:
     * 1. Security monitoring - detecting unusual activity
     * 2. Performance monitoring - tracking event volumes
     * 3. Compliance reporting - demonstrating controls
     * 4. Business intelligence - understanding user behavior
     */
    public Map<String, Object> getAuditStatistics(LocalDateTime from, LocalDateTime to) {
        Map<String, Object> stats = new HashMap<>();

        // Total events
        long totalEvents = auditRepository.countByEventTimestampBetween(from, to);
        stats.put("totalEvents", totalEvents);

        // Events by type
        List<Object[]> eventsByType = auditRepository.countByEventType();
        Map<String, Long> eventsByTypeMap = new HashMap<>();
        for (Object[] row : eventsByType) {
            eventsByTypeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("eventsByType", eventsByTypeMap);

        // Events by source
        List<Object[]> eventsBySource = auditRepository.countByEventSource();
        Map<String, Long> eventsBySourceMap = new HashMap<>();
        for (Object[] row : eventsBySource) {
            eventsBySourceMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("eventsBySource", eventsBySourceMap);

        // Unique customers and loans
        long uniqueCustomers = auditRepository.countDistinctCustomerNumbers();
        long uniqueLoans = auditRepository.countDistinctLoanNumbers();
        stats.put("uniqueCustomers", uniqueCustomers);
        stats.put("uniqueLoans", uniqueLoans);

        stats.put("periodStart", from);
        stats.put("periodEnd", to);

        return stats;
    }
}