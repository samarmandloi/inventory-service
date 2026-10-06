package com.pm.inventoryservice.dto;

import java.util.UUID;

public record VariantEventDto(
        String eventType,
        UUID variantId,
        String sku,
        Integer quantity
) {
}