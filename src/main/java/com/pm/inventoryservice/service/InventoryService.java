package com.pm.inventoryservice.service;

import com.pm.inventoryservice.dto.VariantEventDto;
import com.pm.inventoryservice.entity.Inventory;
import com.pm.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public void processVariantEvent(VariantEventDto event) {

        switch (event.eventType()) {

            case "VARIANT_CREATED", "VARIANT_UPDATED" ->
                    createOrUpdateInventory(event);

            case "VARIANT_DELETED" ->
                    deleteInventory(event);

            default ->
                    throw new IllegalArgumentException(
                            "Unknown variant event type: "
                                    + event.eventType()
                    );
        }
    }

    private void createOrUpdateInventory(
            VariantEventDto event) {

        Inventory inventory =
                inventoryRepository
                        .findByVariantId(event.variantId())
                        .orElseGet(Inventory::new);

        inventory.setVariantId(event.variantId());
        inventory.setSku(event.sku());
        inventory.setQuantity(event.quantity());

        inventoryRepository.save(inventory);
    }

    private void deleteInventory(
            VariantEventDto event) {

        inventoryRepository.deleteByVariantId(
                event.variantId()
        );
    }
}