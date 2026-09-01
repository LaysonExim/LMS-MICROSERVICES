// Purpose: SMS notification service
// File: notification-service/src/main/java/com/nextgenloan/notification/service/SmsService.java

package com.nextgenloan.notification_service.service;

import com.nextgenloan.notification_service.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    /**
     * Send SMS notification
     *
     * WHY: In a real bank, this would integrate with:
     * 1. SMS gateway (Twilio, Vonage, etc.)
     * 2. Short codes for banking services
     * 3. Two-factor authentication
     *
     * For learning, we simulate the sending.
     */
    public void sendSms(NotificationRequest request) {
        log.info("📱 Sending SMS to customer: {}", request.getCustomerNumber());

        try {
            String message = buildSmsMessage(request);
            log.info("  Message: {}", message);

            // Simulate SMS sending
            Thread.sleep(50); // Simulate network delay

            log.info("✅ SMS sent successfully to: {}", request.getCustomerNumber());

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", request.getCustomerNumber(), e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    /**
     * Build SMS message
     */
    private String buildSmsMessage(NotificationRequest request) {
        String message = switch (request.getEventType()) {
            case "LOAN_CREATED" ->
                    String.format("Loan %s: Application received for $%s. Status: %s. We'll notify you of updates.",
                            request.getLoanNumber(), request.getAmount(), request.getStatus());
            case "LOAN_APPROVED" ->
                    String.format("Loan %s: Approved for $%s. Funds will be disbursed in 2-3 business days.",
                            request.getLoanNumber(), request.getAmount());
            case "LOAN_DISBURSED" ->
                    String.format("Loan %s: Disbursed $%s. Check your repayment schedule in online banking.",
                            request.getLoanNumber(), request.getAmount());
            case "LOAN_REPAID" ->
                    String.format("Loan %s: Repayment of $%s received. Thank you for your payment.",
                            request.getLoanNumber(), request.getAmount());
            default ->
                    String.format("Loan %s: Status updated to %s. Please check online banking for details.",
                            request.getLoanNumber(), request.getStatus());
        };

        return "NextGen Loan: " + message;
    }
}