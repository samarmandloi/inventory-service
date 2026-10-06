package com.pm.inventoryservice.controller;

import com.pm.inventoryservice.dto.requestDto.ReservationRequest;
import com.pm.inventoryservice.dto.responseDto.ReservationResponse;
import com.pm.inventoryservice.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(
            @Valid @RequestBody ReservationRequest request
    ) {
        return reservationService.reserve(request);
    }

    @PostMapping("/{requestId}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(
            @PathVariable UUID requestId
    ) {
        reservationService.release(requestId);
    }
}