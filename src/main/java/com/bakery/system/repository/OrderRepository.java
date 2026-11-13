package com.bakery.system.repository;

import com.bakery.system.entity.Order;
import com.bakery.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ========== EXISTING METHODS ==========

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Long countOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // ========== NEW METHODS FOR BETTER PERFORMANCE ==========

    /**
     * Count orders by status (uses database query instead of loading all orders)
     * Example: long count = orderRepository.countByStatus("PENDING");
     *
     * @param status The order status (PENDING, CONFIRMED, PROCESSING, DELIVERED, CANCELLED)
     * @return number of orders with the given status
     */
    long countByStatus(String status);

    /**
     * Find orders by status (returns list of orders with given status)
     *
     * @param status The order status
     * @return List of orders with the given status
     */
    List<Order> findByStatus(String status);

    /**
     * Count orders by multiple statuses
     * Example: long count = orderRepository.countByStatusIn(List.of("PENDING", "CONFIRMED"));
     *
     * @param statuses List of statuses to count
     * @return number of orders with status in the given list
     */
    long countByStatusIn(List<String> statuses);

    /**
     * Find orders by multiple statuses
     *
     * @param statuses List of statuses
     * @return List of orders with status in the given list
     */
    List<Order> findByStatusIn(List<String> statuses);

    /**
     * Count orders by user and status
     *
     * @param user The customer
     * @param status The order status
     * @return number of orders for the user with given status
     */
    long countByUserAndStatus(User user, String status);

    /**
     * Get total revenue from completed orders (DELIVERED status)
     *
     * @return total revenue from delivered orders
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenueFromDeliveredOrders();

    /**
     * Get today's orders count
     *
     * @return number of orders placed today
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderDate) = CURRENT_DATE")
    long countTodayOrders();

    /**
     * Get today's revenue
     *
     * @return total revenue from today's orders
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE DATE(o.orderDate) = CURRENT_DATE")
    Double getTodayRevenue();

    /**
     * Get orders by date range (more efficient than loading all)
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of orders between dates
     */
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count orders by date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return number of orders between dates
     */
    long countByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}