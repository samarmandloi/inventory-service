package com.pm.inventoryservice.controller;

import com.pm.inventoryservice.dto.responseDto.InventoryResponse;
import com.pm.inventoryservice.dto.responseDto.PageResponse;
import com.pm.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public PageResponse<InventoryResponse> getInventory(
            @RequestParam List<String> skus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return inventoryService.getInventory(
                skus,
                pageable
        );
    }
}