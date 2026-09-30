package com.pm.inventoryservice.repository;

import com.pm.inventoryservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository
        extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findByRequestId(UUID requestId);
}