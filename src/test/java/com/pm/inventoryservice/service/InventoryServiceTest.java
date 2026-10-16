package com.pm.inventoryservice.service;

import com.pm.inventoryservice.dto.VariantEventDto;
import com.pm.inventoryservice.entity.Inventory;
import com.pm.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID variantId;

    @BeforeEach
    void setUp() {
        variantId = UUID.randomUUID();
    }

    @Test
    void processVariantEvent_shouldCreateInventory_whenVariantCreated() {

        VariantEventDto event = new VariantEventDto(
                "VARIANT_CREATED",
                variantId,
                "NIKE-AIR-M-RED-001",
                50
        );

        when(inventoryRepository.findByVariantId(variantId))
                .thenReturn(Optional.empty());

        inventoryService.processVariantEvent(event);

        verify(inventoryRepository)
                .findByVariantId(variantId);

        verify(inventoryRepository)
                .save(argThat(inventory ->
                        inventory.getVariantId().equals(variantId)
                                && inventory.getSku().equals("NIKE-AIR-M-RED-001")
                                && inventory.getQuantity() == 50
                ));
    }

    @Test
    void processVariantEvent_shouldUpdateInventory_whenVariantUpdated() {

        Inventory existingInventory = new Inventory();

        existingInventory.setId(UUID.randomUUID());
        existingInventory.setVariantId(variantId);
        existingInventory.setSku("NIKE-AIR-M-RED-001");
        existingInventory.setQuantity(20);

        VariantEventDto event = new VariantEventDto(
                "VARIANT_UPDATED",
                variantId,
                "NIKE-AIR-M-RED-001",
                75
        );

        when(inventoryRepository.findByVariantId(variantId))
                .thenReturn(Optional.of(existingInventory));

        inventoryService.processVariantEvent(event);

        assertEquals(
                variantId,
                existingInventory.getVariantId()
        );

        assertEquals(
                "NIKE-AIR-M-RED-001",
                existingInventory.getSku()
        );

        assertEquals(
                75,
                existingInventory.getQuantity()
        );

        verify(inventoryRepository)
                .findByVariantId(variantId);

        verify(inventoryRepository)
                .save(existingInventory);
    }

    @Test
    void processVariantEvent_shouldDeleteInventory_whenVariantDeleted() {

        VariantEventDto event = new VariantEventDto(
                "VARIANT_DELETED",
                variantId,
                "NIKE-AIR-M-RED-001",
                50
        );

        inventoryService.processVariantEvent(event);

        verify(inventoryRepository)
                .deleteByVariantId(variantId);

        verify(inventoryRepository, never())
                .findByVariantId(any());

        verify(inventoryRepository, never())
                .save(any());
    }

    @Test
    void processVariantEvent_shouldThrowException_whenEventTypeIsUnknown() {

        VariantEventDto event = new VariantEventDto(
                "UNKNOWN_EVENT",
                variantId,
                "NIKE-AIR-M-RED-001",
                50
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> inventoryService.processVariantEvent(event)
                );

        assertEquals(
                "Unknown variant event type: UNKNOWN_EVENT",
                exception.getMessage()
        );

        verifyNoInteractions(inventoryRepository);
    }
}
