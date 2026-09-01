// Purpose: Credit limit business logic
// File: limit-service/src/main/java/com/nextgenloan/limit/service/CreditLimitService.java

package com.nextgenloan.limit_service.service;

import com.nextgenloan.limit_service.dto.CreditLimitRequestDto;
import com.nextgenloan.limit_service.dto.CreditLimitResponseDto;
import com.nextgenloan.limit_service.dto.LimitCheckResponseDto;
import com.nextgenloan.limit_service.entity.CustomerCreditLimit;
import com.nextgenloan.limit_service.entity.LimitReservation;
import com.nextgenloan.limit_service.entity.ProductCreditLimit;
import com.nextgenloan.limit_service.exception.LimitExceededException;
import com.nextgenloan.limit_service.exception.LimitNotFoundException;
import com.nextgenloan.limit_service.exception.ReservationNotFoundException;
import com.nextgenloan.limit_service.mapper.CreditLimitMapper;
import com.nextgenloan.limit_service.repository.CreditLimitRepository;
import com.nextgenloan.limit_service.repository.LimitReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditLimitService {

    private final CreditLimitRepository creditLimitRepository;
    private final LimitReservationRepository reservationRepository;
    private final CreditLimitMapper creditLimitMapper;

    private static final int RESERVATION_EXPIRY_MINUTES = 30;

    /**
     * Check if credit is available and reserve it
     *
     * WHY: This is the core operation for loan applications.
     * It checks if the customer has enough credit and reserves it.
     *
     * PROCESS:
     * 1. Check customer limit
     * 2. Check product limit
     * 3. If available, create a reservation
     * 4. Return reservation ID
     */
    @Transactional
    public LimitCheckResponseDto checkAndReserveCredit(CreditLimitRequestDto requestDto) {
        log.info("Checking credit for customer: {}, product: {}, amount: {}",
                requestDto.getCustomerNumber(), requestDto.getProductType(), requestDto.getAmount());

        // Get customer credit limit
        CustomerCreditLimit customerLimit = creditLimitRepository
                .findByCustomerNumber(requestDto.getCustomerNumber())
                .orElseThrow(() -> new LimitNotFoundException(
                        "No credit limit found for customer: " + requestDto.getCustomerNumber()
                ));

        // Check overall limit
        if (customerLimit.getAvailableLimit().compareTo(requestDto.getAmount()) < 0) {
            log.warn("Insufficient overall credit for customer: {}, available: {}, requested: {}",
                    requestDto.getCustomerNumber(), customerLimit.getAvailableLimit(), requestDto.getAmount());
            return LimitCheckResponseDto.builder()
                    .approved(false)
                    .message("Insufficient overall credit limit")
                    .availableLimit(customerLimit.getAvailableLimit())
                    .requestedAmount(requestDto.getAmount())
                    .build();
        }

        // Check product limit
        ProductCreditLimit productLimit = customerLimit.getProductLimits().stream()
                .filter(p -> p.getProductType().equals(requestDto.getProductType()))
                .findFirst()
                .orElseThrow(() -> new LimitNotFoundException(
                        "No product limit found for: " + requestDto.getProductType()
                ));

        if (productLimit.getAvailableAmount().compareTo(requestDto.getAmount()) < 0) {
            log.warn("Insufficient product credit for customer: {}, product: {}, available: {}, requested: {}",
                    requestDto.getCustomerNumber(), requestDto.getProductType(),
                    productLimit.getAvailableAmount(), requestDto.getAmount());
            return LimitCheckResponseDto.builder()
                    .approved(false)
                    .message("Insufficient product credit limit")
                    .availableLimit(productLimit.getAvailableAmount())
                    .requestedAmount(requestDto.getAmount())
                    .build();
        }

        // Create reservation
        String reservationId = generateReservationId();
        LimitReservation reservation = LimitReservation.builder()
                .reservationId(reservationId)
                .customerNumber(requestDto.getCustomerNumber())
                .productType(requestDto.getProductType())
                .amount(requestDto.getAmount())
                .loanNumber(requestDto.getLoanNumber())
                .status("PENDING")
                .expiryDate(LocalDateTime.now().plusMinutes(RESERVATION_EXPIRY_MINUTES))
                .build();

        reservationRepository.save(reservation);

        // Reduce available credit (temporarily)
        customerLimit.setAvailableLimit(
                customerLimit.getAvailableLimit().subtract(requestDto.getAmount())
        );
        productLimit.setAvailableAmount(
                productLimit.getAvailableAmount().subtract(requestDto.getAmount())
        );

        creditLimitRepository.save(customerLimit);

        log.info("Credit reserved: {}, reservation: {}", reservationId, requestDto.getCustomerNumber());

        return LimitCheckResponseDto.builder()
                .approved(true)
                .message("Credit reserved successfully")
                .availableLimit(customerLimit.getAvailableLimit())
                .requestedAmount(requestDto.getAmount())
                .reservationId(reservationId)
                .build();
    }

    /**
     * Confirm a reservation (permanent deduction)
     *
     * WHY: After loan approval, we make the reservation permanent.
     */
    @Transactional
    public void confirmReservation(String reservationId) {
        log.info("Confirming reservation: {}", reservationId);

        LimitReservation reservation = reservationRepository
                .findByReservationId(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId
                ));

        if (!"PENDING".equals(reservation.getStatus())) {
            throw new IllegalStateException(
                    "Reservation is not in PENDING state: " + reservation.getStatus()
            );
        }

        // Update reservation
        reservation.setStatus("CONFIRMED");
        reservation.setConfirmedDate(LocalDateTime.now());
        reservationRepository.save(reservation);

        // Update used limit
        CustomerCreditLimit customerLimit = creditLimitRepository
                .findByCustomerNumber(reservation.getCustomerNumber())
                .orElseThrow(() -> new LimitNotFoundException(
                        "Customer not found: " + reservation.getCustomerNumber()
                ));

        customerLimit.setUsedLimit(
                customerLimit.getUsedLimit().add(reservation.getAmount())
        );
        customerLimit.setLastUpdated(LocalDateTime.now());
        creditLimitRepository.save(customerLimit);

        // Update product used amount
        customerLimit.getProductLimits().stream()
                .filter(p -> p.getProductType().equals(reservation.getProductType()))
                .findFirst()
                .ifPresent(p -> {
                    p.setUsedAmount(p.getUsedAmount().add(reservation.getAmount()));
                    p.setAvailableAmount(p.getAvailableAmount());
                });

        log.info("Reservation confirmed: {}", reservationId);
    }

    /**
     * Release a reservation (when loan is cancelled or rejected)
     *
     * WHY: If the loan doesn't proceed, we release the reserved credit.
     */
    @Transactional
    public void releaseReservation(String reservationId) {
        log.info("Releasing reservation: {}", reservationId);

        LimitReservation reservation = reservationRepository
                .findByReservationId(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId
                ));

        if (!"PENDING".equals(reservation.getStatus())) {
            throw new IllegalStateException(
                    "Reservation is not in PENDING state: " + reservation.getStatus()
            );
        }

        // Update reservation
        reservation.setStatus("RELEASED");
        reservation.setReleasedDate(LocalDateTime.now());
        reservationRepository.save(reservation);

        // Add back available credit
        CustomerCreditLimit customerLimit = creditLimitRepository
                .findByCustomerNumber(reservation.getCustomerNumber())
                .orElseThrow(() -> new LimitNotFoundException(
                        "Customer not found: " + reservation.getCustomerNumber()
                ));

        customerLimit.setAvailableLimit(
                customerLimit.getAvailableLimit().add(reservation.getAmount())
        );
        customerLimit.setLastUpdated(LocalDateTime.now());
        creditLimitRepository.save(customerLimit);

        // Update product available amount
        customerLimit.getProductLimits().stream()
                .filter(p -> p.getProductType().equals(reservation.getProductType()))
                .findFirst()
                .ifPresent(p -> {
                    p.setAvailableAmount(p.getAvailableAmount().add(reservation.getAmount()));
                });

        log.info("Reservation released: {}", reservationId);
    }

    /**
     * Get customer credit limit
     */
    public CreditLimitResponseDto getCustomerLimit(String customerNumber) {
        CustomerCreditLimit customerLimit = creditLimitRepository
                .findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new LimitNotFoundException(
                        "No credit limit found for customer: " + customerNumber
                ));

        return creditLimitMapper.toResponseDto(customerLimit);
    }

    /**
     * Get reservation status
     */
    public LimitReservation getReservation(String reservationId) {
        return reservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId
                ));
    }

    /**
     * Clean expired reservations
     *
     * WHY: This is a scheduled job that releases expired reservations.
     * If a reservation expires, we release the credit.
     */
    @Transactional
    public void cleanExpiredReservations() {
        log.info("Cleaning expired reservations");

        LocalDateTime expiryThreshold = LocalDateTime.now();
        List<LimitReservation> expiredReservations = reservationRepository
                .findByStatusAndExpiryDateBefore("PENDING", expiryThreshold);

        if (expiredReservations.isEmpty()) {
            log.debug("No expired reservations found");
            return;
        }

        log.info("Found {} expired reservations", expiredReservations.size());

        for (LimitReservation reservation : expiredReservations) {
            try {
                releaseReservation(reservation.getReservationId());
                log.info("Released expired reservation: {}", reservation.getReservationId());
            } catch (Exception e) {
                log.error("Failed to release expired reservation: {}",
                        reservation.getReservationId(), e);
            }
        }
    }

    /**
     * Generate reservation ID
     */
    private String generateReservationId() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}