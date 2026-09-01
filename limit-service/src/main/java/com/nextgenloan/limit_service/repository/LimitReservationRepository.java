// Purpose: Repository interface for LimitReservation entity
// File: limit-service/src/main/java/com/nextgenloan/limit/repository/LimitReservationRepository.java
// Dependencies: Spring Data JPA

package com.nextgenloan.limit_service.repository;

import com.nextgenloan.limit_service.entity.LimitReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LimitReservationRepository extends JpaRepository<LimitReservation, Long> {

    Optional<LimitReservation> findByReservationId(String reservationId);

    List<LimitReservation> findByStatusAndExpiryDateBefore(String status, LocalDateTime expiryDate);
}
