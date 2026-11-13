package com.bakery.system.repository;

import com.bakery.system.entity.StockMovement;
import com.bakery.system.dto.MonthlySummaryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT s FROM StockMovement s WHERE s.movementDate BETWEEN :startDate AND :endDate")
    List<StockMovement> findByMovementDateBetween(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockMovement s WHERE s.product.id = :productId AND s.movementType = 'OUT' AND s.movementDate BETWEEN :startDate AND :endDate")
    Integer getTotalSoldForProduct(@Param("productId") Long productId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockMovement s WHERE s.movementType = 'OUT' AND s.movementDate BETWEEN :startDate AND :endDate")
    Integer getTotalSoldAllProducts(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockMovement s WHERE s.movementType = 'IN' AND s.movementDate BETWEEN :startDate AND :endDate")
    Integer getTotalRestockedAllProducts(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockMovement s WHERE s.product.id = :productId AND s.movementType = 'IN' AND s.movementDate BETWEEN :startDate AND :endDate")
    Integer getTotalRestockedForProduct(@Param("productId") Long productId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.bakery.system.dto.MonthlySummaryDTO(" +
            "YEAR(s.movementDate), " +
            "MONTH(s.movementDate), " +
            "SUM(CASE WHEN s.movementType = 'OUT' THEN s.quantity ELSE 0 END), " +
            "SUM(CASE WHEN s.movementType = 'IN' THEN s.quantity ELSE 0 END)) " +
            "FROM StockMovement s " +
            "WHERE s.movementDate >= :startDate " +
            "GROUP BY YEAR(s.movementDate), MONTH(s.movementDate) " +
            "ORDER BY YEAR(s.movementDate) DESC, MONTH(s.movementDate) DESC")
    List<MonthlySummaryDTO> getMonthlySummary(@Param("startDate") LocalDateTime startDate);
}