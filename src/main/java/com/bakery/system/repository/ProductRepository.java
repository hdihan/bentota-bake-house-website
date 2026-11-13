package com.bakery.system.repository;

import com.bakery.system.entity.Product;
import com.bakery.system.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ========== EXISTING METHODS ==========

    List<Product> findByCategory(Category category);

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    List<Product> findByExpirationDateBetween(LocalDate start, LocalDate end);

    // ========== NEW METHODS FOR BETTER PERFORMANCE ==========

    /**
     * Count products with low stock (stock < threshold)
     *
     * @param threshold Stock threshold (e.g., 5)
     * @return number of products with stock below threshold
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity < :threshold")
    long countLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * Find products with low stock
     *
     * @param threshold Stock threshold
     * @return List of products with low stock
     */
    List<Product> findByStockQuantityLessThan(Integer threshold);

    /**
     * Count out of stock products (stock = 0 or null)
     *
     * @return number of out of stock products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity IS NULL OR p.stockQuantity = 0")
    long countOutOfStockProducts();

    /**
     * Count active products
     *
     * @return number of active products
     */
    long countByStatus(String status);

    /**
     * Find products expiring between dates (more efficient)
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of expiring products
     */
    List<Product> findByExpirationDateBetweenOrderByExpirationDateAsc(LocalDate startDate, LocalDate endDate);

    /**
     * Count products expiring in next N days
     *
     * @param today Today's date
     * @param threshold Future date
     * @return number of products expiring soon
     */
    long countByExpirationDateBetween(LocalDate today, LocalDate threshold);

    /**
     * Get total inventory value (sum of stock * price)
     *
     * @return total inventory value
     */
    @Query("SELECT COALESCE(SUM(p.stockQuantity * p.price), 0) FROM Product p")
    Double getTotalInventoryValue();
}