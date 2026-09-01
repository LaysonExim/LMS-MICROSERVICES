// Purpose: Email notification service
// File: notification-service/src/main/java/com/nextgenloan/notification/service/EmailService.java

package com.nextgenloan.notification_service.service;

import com.nextgenloan.notification_service.dto.NotificationRequest;
import com.nextgenloan.notification_service.template.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final NotificationTemplate notificationTemplate;

    /**
     * Send email notification
     *
     * WHY: In a real bank, this would integrate with:
     * 1. SMTP server for emails
     * 2. SMS gateway for texts
     * 3. Push notification service for mobile
     *
     * For learning, we simulate the sending.
     */
    public void sendEmail(NotificationRequest request) {
        log.info("📧 Sending email to: {}", request.getEmail());

        try {
            // Build email content
            Map<String, Object> context = buildEmailContext(request);
            String subject = notificationTemplate.getSubject(request.getEventType());
            String body = notificationTemplate.renderBody(request.getEventType(), context);

            // In a real system, this would send via SMTP
            log.info("  Subject: {}", subject);
            log.info("  Body: {}", body);

            // Simulate email sending
            Thread.sleep(100); // Simulate network delay

            log.info("✅ Email sent successfully to: {}", request.getEmail());

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * Build email context
     */
    private Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("loanNumber", request.getLoanNumber());
        context.put("customerNumber", request.getCustomerNumber());
        context.put("firstName", request.getFirstName());
        context.put("lastName", request.getLastName());
        context.put("amount", request.getAmount());
        context.put("loanType", request.getLoanType());
        context.put("status", request.getStatus());
        context.put("timestamp", request.getEventTimestamp());

        if (request.getAdditionalData() != null) {
            context.putAll(request.getAdditionalData());
        }

        return context;
    }
}