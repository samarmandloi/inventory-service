package com.pm.inventoryservice.repository;

import com.pm.inventoryservice.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByVariantId(UUID variantId);

    void deleteByVariantId(UUID variantId);

    Page<Inventory> findBySkuIn(
            List<String> skus,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findBySku(String sku);
}