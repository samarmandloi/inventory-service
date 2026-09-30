package com.pm.inventoryservice.service;

import com.pm.inventoryservice.dto.VariantEventDto;
import com.pm.inventoryservice.dto.responseDto.PageResponse;
import com.pm.inventoryservice.entity.Inventory;
import com.pm.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
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

    public PageResponse<Map<String, Object>> getInventory(
            List<String> skus,
            Pageable pageable
    ) {
        Page<Inventory> inventoryPage =
                inventoryRepository.findBySkuIn(
                        skus,
                        pageable
                );

        List<Map<String, Object>> content =
                inventoryPage.getContent()
                        .stream()
                        .map(inventory -> {

                            Map<String, Object> map = new HashMap<>();

                            map.put("sku", inventory.getSku());

                            map.put(
                                    "availableQuantity",
                                    inventory.getQuantity()
                                            - inventory.getReservedQuantity()
                            );

                            return map;
                        })
                        .toList();

        return new PageResponse<>(
                content,
                inventoryPage.getNumber(),
                inventoryPage.getSize(),
                inventoryPage.getTotalElements(),
                inventoryPage.getTotalPages()
        );
    }
}