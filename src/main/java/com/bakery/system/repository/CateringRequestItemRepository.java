package com.bakery.system.repository;

import com.bakery.system.entity.CateringRequestItem;
import com.bakery.system.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CateringRequestItemRepository extends JpaRepository<CateringRequestItem, Long> {
    boolean existsByProduct(Product product);
}