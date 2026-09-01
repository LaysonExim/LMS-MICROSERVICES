package com.nextgenloan.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String eventType;
    private String loanNumber;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal amount;
    private String loanType;
    private String status;
    private LocalDateTime eventTimestamp;
    private Map<String, Object> additionalData;
}
