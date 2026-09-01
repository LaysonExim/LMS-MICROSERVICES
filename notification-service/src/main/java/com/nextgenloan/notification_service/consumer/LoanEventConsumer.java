// Purpose: Enhanced consumer with error handling
// File: notification-service/src/main/java/com/nextgenloan/notification/consumer/LoanEventConsumer.java

package com.nextgenloan.notification_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextgenloan.notification_service.dto.NotificationRequest;
import com.nextgenloan.notification_service.event.LoanEvent;
import com.nextgenloan.notification_service.service.EmailService;
import com.nextgenloan.notification_service.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventConsumer {

    private final EmailService emailService;
    private final SmsService smsService;
    private final ObjectMapper objectMapper;

    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    /**
     * Consume loan events with error handling
     *
     * WHY: Even non-critical services need to be reliable.
     *
     * ERROR HANDLING:
     * 1. Retry failed events up to 3 times
     * 2. If still failing, log to dead letter queue
     * 3. Manual intervention for critical failures
     *
     * In a real bank, this would also:
     * 1. Send alerts for repeated failures
     * 2. Track error rates and thresholds
     * 3. Automatic circuit breaking
     * 4. Fallback mechanisms
     */
    @KafkaListener(topics = "loan-events", groupId = "notification-service-group")
    public void consumeLoanEvent(String eventJson, Acknowledgment acknowledgment) {
        try {
            log.debug("Notification: Processing event: {}", eventJson);

            // Parse event
            LoanEvent event = objectMapper.readValue(eventJson, LoanEvent.class);

            log.info("Notification: Received event: {} for loan: {}",
                    event.getEventType(), event.getLoanNumber());

            // Process event
            processEvent(event);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

            log.info("Notification: Processed event: {} for loan: {}",
                    event.getEventType(), event.getLoanNumber());

            // Reset retry count on success
            retryCount = 0;

        } catch (Exception e) {
            log.error("Notification: Failed to process event: {}", e.getMessage(), e);

            // Retry logic
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                log.warn("Notification: Retry attempt {} for event", retryCount);
                // Re-throw to retry
                throw new RuntimeException("Event processing failed, will retry", e);
            } else {
                log.error("Notification: Max retries exceeded, moving to dead letter queue");
                // In a real system, we'd send to DLQ
                // For now, we just log
                retryCount = 0;
                acknowledgment.acknowledge(); // Acknowledge to avoid infinite retries
            }
        }
    }

    /**
     * Process event and send notifications
     */
    private void processEvent(LoanEvent event) {
        NotificationRequest request = buildNotificationRequest(event);

        // Send email
        try {
            emailService.sendEmail(request);
        } catch (Exception e) {
            log.error("Email failed, continuing with SMS: {}", e.getMessage());
            // Try SMS as fallback
        }

        // Send SMS
        try {
            smsService.sendSms(request);
        } catch (Exception e) {
            log.error("SMS failed: {}", e.getMessage());
            // If both email and SMS fail, log and continue
        }
    }

    /**
     * Build notification request from event
     */
    private NotificationRequest buildNotificationRequest(LoanEvent event) {
        return NotificationRequest.builder()
                .eventType(event.getEventType())
                .loanNumber(event.getLoanNumber())
                .customerNumber(event.getCustomerNumber())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .email(event.getEmail())
                .amount(event.getAmount())
                .loanType(event.getLoanType())
                .status(extractStatus(event))
                .eventTimestamp(event.getEventTimestamp())
                .additionalData(extractAdditionalData(event))
                .build();
    }

    private String extractStatus(LoanEvent event) {
        // Extract status from event
        if (event.getAdditionalData() != null) {
            String[] parts = event.getAdditionalData().split(",");
            for (String part : parts) {
                if (part.startsWith("newStatus=")) {
                    return part.substring(10);
                }
            }
        }
        return "UNKNOWN";
    }

    private Map<String, Object> extractAdditionalData(LoanEvent event) {
        Map<String, Object> data = new HashMap<>();
        if (event.getAdditionalData() != null) {
            String[] parts = event.getAdditionalData().split(",");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    data.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return data;
    }
}