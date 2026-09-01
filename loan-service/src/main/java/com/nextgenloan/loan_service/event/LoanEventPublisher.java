// Purpose: Version-aware event publisher
// File: loan-service/src/main/java/com/nextgenloan/loan/event/LoanEventPublisher.java

package com.nextgenloan.loan_service.event;

import com.nextgenloan.loan_service.dto.LoanResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String LOAN_TOPIC = "loan-events";

    /**
     * Publish loan created event (Version 2.0)
     *
     * WHY: Event versioning allows schema evolution.
     *
     * BACKWARD COMPATIBILITY:
     * - All consumers can handle v2.0 events
     * - v1.0 consumers will ignore new fields
     * - v2.0 consumers will use all fields
     *
     * This is the recommended approach for banking systems.
     */
    public void publishLoanCreatedEvent(LoanResponseDto loan) {
        LoanEvent event = LoanEvent.builder()
                .eventType("LOAN_CREATED")
                .eventVersion("2.0")
                .loanNumber(loan.getLoanNumber())
                .customerNumber(loan.getCustomerNumber())
                .loanType(loan.getLoanType())
                .loanPurpose(loan.getLoanPurpose())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .firstName(loan.getFirstName())
                .lastName(loan.getLastName())
                .email(loan.getEmail())
                .correlationId(UUID.randomUUID().toString())
                .eventTimestamp(LocalDateTime.now())
                .eventSource("loan-service")
                .additionalData("status=" + loan.getStatus())
                .build();

        // Publish to Kafka
        kafkaTemplate.send(LOAN_TOPIC, loan.getLoanNumber(), event);

        log.info("Published LOAN_CREATED event (v2.0) for loan: {}", loan.getLoanNumber());
    }

    /**
     * Publish loan status changed event (Version 2.0)
     */
    public void publishLoanStatusChangedEvent(String loanNumber, String oldStatus, String newStatus) {
        LoanEvent event = LoanEvent.builder()
                .eventType("LOAN_STATUS_CHANGED")
                .eventVersion("2.0")
                .loanNumber(loanNumber)
                .correlationId(UUID.randomUUID().toString())
                .eventTimestamp(LocalDateTime.now())
                .eventSource("loan-service")
                .additionalData("oldStatus=" + oldStatus + ",newStatus=" + newStatus)
                .build();

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);

        log.info("Published LOAN_STATUS_CHANGED event (v2.0) for loan: {} ({} → {})",
                loanNumber, oldStatus, newStatus);
    }

    /**
     * Publish loan disbursed event (Version 2.0)
     */
    public void publishLoanDisbursedEvent(String loanNumber) {
        LoanEvent event = LoanEvent.builder()
                .eventType("LOAN_DISBURSED")
                .eventVersion("2.0")
                .loanNumber(loanNumber)
                .correlationId(UUID.randomUUID().toString())
                .eventTimestamp(LocalDateTime.now())
                .eventSource("loan-service")
                .build();

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("Published LOAN_DISBURSED event (v2.0) for loan: {}", loanNumber);
    }

    /**
     * Publish loan repaid event (Version 2.0)
     */
    public void publishLoanRepaidEvent(String loanNumber, BigDecimal amount) {
        LoanEvent event = LoanEvent.builder()
                .eventType("LOAN_REPAID")
                .eventVersion("2.0")
                .loanNumber(loanNumber)
                .amount(amount)
                .correlationId(UUID.randomUUID().toString())
                .eventTimestamp(LocalDateTime.now())
                .eventSource("loan-service")
                .additionalData("repaymentAmount=" + amount)
                .build();

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("Published LOAN_REPAID event (v2.0) for loan: {}", loanNumber);
    }
}