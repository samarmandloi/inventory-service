package com.pm.inventoryservice.dto.requestDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationItem(

        @NotBlank
        String sku,

        @NotNull
        @Min(1)
        Integer quantity

) {
}