package com.pm.inventoryservice.repository;

import com.pm.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByVariantId(UUID variantId);

    void deleteByVariantId(UUID variantId);
}