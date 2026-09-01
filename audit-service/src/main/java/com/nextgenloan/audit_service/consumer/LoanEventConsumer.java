// Purpose: Consumes loan events and stores them
// File: audit-service/src/main/java/com/nextgenloan/audit/consumer/LoanEventConsumer.java

package com.nextgenloan.audit_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextgenloan.audit_service.dto.LoanEvent;
import com.nextgenloan.audit_service.entity.AuditEvent;
import com.nextgenloan.audit_service.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanEventConsumer {

    private final AuditEventRepository auditRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consume loan events from Kafka and store them
     *
     * WHY: Every event is stored for compliance and auditing.
     *
     * This is the "single source of truth" for all system activities.
     * In a real bank, this data would be:
     * 1. Immutable - never deleted or modified
     * 2. Time-stamped - precise audit trail
     * 3. Queryable - for regulatory reports
     * 4. Encrypted - for security
     * 5. Replicated - for disaster recovery
     */
    @KafkaListener(topics = "loan-events", groupId = "audit-service-group")
    @Transactional
    public void consumeLoanEvent(String eventJson) {
        try {
            // Parse the event
            LoanEvent event = objectMapper.readValue(eventJson, LoanEvent.class);

            log.info("Audit: Received event: {} for loan: {}",
                    event.getEventType(), event.getLoanNumber());

            // Create audit record
            AuditEvent auditEvent = AuditEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(event.getEventType())
                    .eventSource(event.getEventSource())
                    .eventTimestamp(event.getEventTimestamp())
                    .correlationId(event.getCorrelationId())
                    .loanNumber(event.getLoanNumber())
                    .customerNumber(event.getCustomerNumber())
                    .amount(event.getAmount())
                    .additionalData(buildAdditionalData(event))
                    .createdAt(LocalDateTime.now())
                    .build();

            // Store in database
            auditRepository.save(auditEvent);

            log.info("Audit: Stored event: {} for loan: {}",
                    event.getEventType(), event.getLoanNumber());

        } catch (Exception e) {
            log.error("Audit: Failed to process event: {}", e.getMessage(), e);
        }
    }

    /**
     * Build additional data from event
     *
     * WHY: We store all event data for flexibility.
     * This allows us to query specific fields without
     * changing the schema.
     */
    private Map<String, Object> buildAdditionalData(LoanEvent event) {
        Map<String, Object> data = new HashMap<>();

        data.put("loanType", event.getLoanType());
        data.put("firstName", event.getFirstName());
        data.put("lastName", event.getLastName());
        data.put("email", event.getEmail());
        data.put("interestRate", event.getInterestRate());
        data.put("termMonths", event.getTermMonths());
        data.put("eventVersion", event.getEventVersion());

        if (event.getAdditionalData() != null) {
            data.put("additionalData", event.getAdditionalData());
        }

        return data;
    }
}