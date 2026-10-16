package com.pm.inventoryservice.service;

import com.pm.inventoryservice.dto.requestDto.ReservationItem;
import com.pm.inventoryservice.entity.Inventory;
import com.pm.inventoryservice.entity.Reservation;
import com.pm.inventoryservice.entity.ReservationStatus;
import com.pm.inventoryservice.repository.InventoryRepository;
import com.pm.inventoryservice.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReservationService reservationService;

    private UUID requestId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();

        inventory = new Inventory();
        inventory.setSku("IPHONE-15");
        inventory.setQuantity(100);
        inventory.setReservedQuantity(5);
    }

    @Test
    void shouldAcceptReservation() throws Exception {

        Reservation reservation = new Reservation();

        reservation.setRequestId(requestId);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setItems("reservation-items");

        ReservationItem item =
                new ReservationItem(
                        "IPHONE-15",
                        5
                );

        when(reservationRepository.findByRequestId(requestId))
                .thenReturn(Optional.of(reservation));

        when(objectMapper.readValue(
                eq("reservation-items"),
                any(TypeReference.class)
        )).thenReturn(List.of(item));

        when(inventoryRepository.findBySku("IPHONE-15"))
                .thenReturn(Optional.of(inventory));

        reservationService.accept(requestId);

        assertEquals(
                95,
                inventory.getQuantity()
        );

        assertEquals(
                0,
                inventory.getReservedQuantity()
        );

        assertEquals(
                ReservationStatus.ACCEPTED,
                reservation.getStatus()
        );

        verify(inventoryRepository)
                .save(inventory);

        verify(reservationRepository)
                .save(reservation);
    }

    @Test
    void shouldNotAcceptReleasedReservation() {

        Reservation reservation = new Reservation();

        reservation.setRequestId(requestId);
        reservation.setStatus(ReservationStatus.RELEASED);

        when(reservationRepository.findByRequestId(requestId))
                .thenReturn(Optional.of(reservation));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService.accept(requestId)
                );

        assertEquals(
                "Cannot accept a released reservation: " + requestId,
                exception.getMessage()
        );

        verify(inventoryRepository, never())
                .save(any());

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void shouldNotAcceptAlreadyAcceptedReservation() {

        Reservation reservation = new Reservation();

        reservation.setRequestId(requestId);
        reservation.setStatus(ReservationStatus.ACCEPTED);

        when(reservationRepository.findByRequestId(requestId))
                .thenReturn(Optional.of(reservation));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService.accept(requestId)
                );

        assertEquals(
                "Reservation is already accepted: " + requestId,
                exception.getMessage()
        );

        verify(inventoryRepository, never())
                .save(any());

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void shouldNotReleaseAcceptedReservation() {

        Reservation reservation = new Reservation();

        reservation.setRequestId(requestId);
        reservation.setStatus(ReservationStatus.ACCEPTED);

        when(reservationRepository.findByRequestId(requestId))
                .thenReturn(Optional.of(reservation));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService.release(requestId)
                );

        assertEquals(
                "Cannot release an accepted reservation: " + requestId,
                exception.getMessage()
        );

        verify(inventoryRepository, never())
                .save(any());

        verify(reservationRepository, never())
                .save(any());
    }
}