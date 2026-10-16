package com.pm.inventoryservice.dto.responseDto;

public record InventoryResponse(
        String sku,
        Integer availableQuantity
) {
}
