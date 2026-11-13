package com.bakery.system.repository;

import com.bakery.system.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByExpirationDateBetween(LocalDate start, LocalDate end);
}