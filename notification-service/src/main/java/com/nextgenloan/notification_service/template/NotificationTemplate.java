// Purpose: Email and SMS templates
// File: notification-service/src/main/java/com/nextgenloan/notification/template/NotificationTemplate.java

package com.nextgenloan.notification_service.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class NotificationTemplate {

    /**
     * Get email subject for event type
     */
    public String getSubject(String eventType) {
        return switch (eventType) {
            case "LOAN_CREATED" -> "Your Loan Application Has Been Received";
            case "LOAN_VERIFIED" -> "Your Loan Application Has Been Verified";
            case "LOAN_APPROVED" -> "Congratulations! Your Loan Has Been Approved";
            case "LOAN_DISBURSED" -> "Your Loan Has Been Disbursed";
            case "LOAN_REPAID" -> "Your Loan Repayment Has Been Received";
            case "LOAN_STATUS_CHANGED" -> "Your Loan Status Has Been Updated";
            default -> "Bank Notification";
        };
    }

    /**
     * Render email body
     */
    public String renderBody(String eventType, Map<String, Object> context) {
        return switch (eventType) {
            case "LOAN_CREATED" -> renderLoanCreated(context);
            case "LOAN_APPROVED" -> renderLoanApproved(context);
            case "LOAN_DISBURSED" -> renderLoanDisbursed(context);
            case "LOAN_REPAID" -> renderLoanRepaid(context);
            default -> renderGenericNotification(context);
        };
    }

    private String renderLoanCreated(Map<String, Object> context) {
        String firstName = (String) context.getOrDefault("firstName", "Customer");
        String loanNumber = (String) context.getOrDefault("loanNumber", "Unknown");
        String amount = context.getOrDefault("amount", "0").toString();
        String loanType = (String) context.getOrDefault("loanType", "Unknown");
        String status = (String) context.getOrDefault("status", "PENDING");

        return String.format("""
            Dear %s,
            
            Thank you for your loan application.
            
            Loan Details:
            ------------
            Loan Number: %s
            Loan Type: %s
            Amount: $%s
            Status: %s
            
            Your application is now being processed. We will notify you when there is an update.
            
            If you have any questions, please contact our customer service team.
            
            Best regards,
            NextGen Loan Management Platform
            """,
                firstName, loanNumber, loanType, amount, status);
    }

    private String renderLoanApproved(Map<String, Object> context) {
        String firstName = (String) context.getOrDefault("firstName", "Customer");
        String loanNumber = (String) context.getOrDefault("loanNumber", "Unknown");
        String amount = context.getOrDefault("amount", "0").toString();

        return String.format("""
            Dear %s,
            
            Congratulations! Your loan has been approved.
            
            Loan Number: %s
            Approved Amount: $%s
            
            The funds will be disbursed to your account within 2-3 business days.
            
            Best regards,
            NextGen Loan Management Platform
            """,
                firstName, loanNumber, amount);
    }

    private String renderLoanDisbursed(Map<String, Object> context) {
        String firstName = (String) context.getOrDefault("firstName", "Customer");
        String loanNumber = (String) context.getOrDefault("loanNumber", "Unknown");
        String amount = context.getOrDefault("amount", "0").toString();

        return String.format("""
            Dear %s,
            
            Your loan has been disbursed.
            
            Loan Number: %s
            Disbursed Amount: $%s
            
            The funds have been transferred to your account.
            
            Please note your repayment schedule will be available in your online banking portal.
            
            Best regards,
            NextGen Loan Management Platform
            """,
                firstName, loanNumber, amount);
    }

    private String renderLoanRepaid(Map<String, Object> context) {
        String firstName = (String) context.getOrDefault("firstName", "Customer");
        String loanNumber = (String) context.getOrDefault("loanNumber", "Unknown");
        String amount = context.getOrDefault("amount", "0").toString();
        String balance = context.getOrDefault("balance", "0").toString();

        return String.format("""
            Dear %s,
            
            We have received your loan repayment.
            
            Loan Number: %s
            Payment Amount: $%s
            Remaining Balance: $%s
            
            Thank you for your payment.
            
            Best regards,
            NextGen Loan Management Platform
            """,
                firstName, loanNumber, amount, balance);
    }

    private String renderGenericNotification(Map<String, Object> context) {
        String firstName = (String) context.getOrDefault("firstName", "Customer");
        String loanNumber = (String) context.getOrDefault("loanNumber", "Unknown");
        String status = (String) context.getOrDefault("status", "Updated");

        return String.format("""
            Dear %s,
            
            Your loan status has been updated.
            
            Loan Number: %s
            New Status: %s
            
            Please log in to your account for more details.
            
            Best regards,
            NextGen Loan Management Platform
            """,
                firstName, loanNumber, status);
    }
}