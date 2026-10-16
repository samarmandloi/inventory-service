package com.pm.inventoryservice.dto.responseDto;

import com.pm.inventoryservice.dto.requestDto.ReservationItem;
import com.pm.inventoryservice.entity.ReservationStatus;

import java.util.List;
import java.util.UUID;

public record ReservationResponse(
        UUID requestId,
        List<ReservationItem> items,
        ReservationStatus status
) {
}