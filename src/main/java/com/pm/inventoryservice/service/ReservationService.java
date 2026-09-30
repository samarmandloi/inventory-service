package com.pm.inventoryservice.service;


import com.pm.inventoryservice.dto.requestDto.ReservationItem;
import com.pm.inventoryservice.dto.requestDto.ReservationRequest;
import com.pm.inventoryservice.dto.responseDto.ReservationResponse;
import com.pm.inventoryservice.entity.Inventory;
import com.pm.inventoryservice.entity.Reservation;
import com.pm.inventoryservice.entity.ReservationStatus;
import com.pm.inventoryservice.repository.InventoryRepository;
import com.pm.inventoryservice.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {

        List<ReservationItem> items = request.items();

        Set<String> skus = new HashSet<>();

        for (ReservationItem item : items) {

            if (!skus.add(item.sku())) {
                throw new IllegalArgumentException(
                        "Duplicate SKU in reservation: " + item.sku()
                );
            }
        }

        Map<String, Inventory> inventories = new HashMap<>();

        for (ReservationItem item : items) {

            Inventory inventory =
                    inventoryRepository
                            .findBySku(item.sku())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory not found for SKU: "
                                                    + item.sku()
                                    )
                            );

            int availableQuantity =
                    inventory.getQuantity()
                            - inventory.getReservedQuantity();

            if (availableQuantity < item.quantity()) {
                throw new IllegalArgumentException(
                        "Insufficient inventory for SKU: "
                                + item.sku()
                );
            }

            inventories.put(item.sku(), inventory);
        }

        for (ReservationItem item : items) {

            Inventory inventory = inventories.get(item.sku());

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity()
                            + item.quantity()
            );

            inventoryRepository.save(inventory);
        }

        Reservation reservation = new Reservation();

        reservation.setRequestId(request.requestId());

        reservation.setItems(
                objectMapper.writeValueAsString(items)
        );

        reservation.setStatus(
                ReservationStatus.RESERVED
        );

        reservationRepository.save(reservation);

        return new ReservationResponse(
                reservation.getRequestId(),
                items,
                reservation.getStatus()
        );
    }

    @Transactional
    public void release(java.util.UUID requestId) {

        Reservation reservation =
                reservationRepository
                        .findByRequestId(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found: "
                                                + requestId
                                )
                        );

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            throw new IllegalArgumentException(
                    "Reservation is already released: "
                            + requestId
            );
        }

        List<ReservationItem> items;

        try {
            items = objectMapper.readValue(
                    reservation.getItems(),
                    new TypeReference<List<ReservationItem>>() {}
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to read reservation items",
                    e
            );
        }

        for (ReservationItem item : items) {

            Inventory inventory =
                    inventoryRepository
                            .findBySku(item.sku())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory not found for SKU: "
                                                    + item.sku()
                                    )
                            );

            int newReservedQuantity =
                    inventory.getReservedQuantity()
                            - item.quantity();

            if (newReservedQuantity < 0) {
                throw new IllegalStateException(
                        "Reserved quantity cannot become negative for SKU: "
                                + item.sku()
                );
            }

            inventory.setReservedQuantity(
                    newReservedQuantity
            );

            inventoryRepository.save(inventory);
        }

        reservation.setStatus(
                ReservationStatus.RELEASED
        );

        reservationRepository.save(reservation);
    }
}