package com.pm.inventoryservice.dto.requestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReservationRequest(

        @NotNull
        UUID requestId,

        @NotEmpty
        List<@Valid ReservationItem> items

) {
}