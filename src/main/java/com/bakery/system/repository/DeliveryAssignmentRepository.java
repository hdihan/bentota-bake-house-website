package com.bakery.system.repository;

import com.bakery.system.entity.DeliveryAssignment;
import com.bakery.system.entity.DeliveryPersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {

    // ==================== BASIC FIND METHODS ====================

    /**
     * Find all assignments for a specific delivery personnel
     */
    List<DeliveryAssignment> findByDeliveryPersonnel(DeliveryPersonnel personnel);

    /**
     * Find all assignments by status
     */
    List<DeliveryAssignment> findByStatus(String status);

    /**
     * Find assignments by delivery personnel and multiple statuses (for driver dashboard)
     */
    List<DeliveryAssignment> findByDeliveryPersonnelAndStatusIn(DeliveryPersonnel personnel, List<String> statuses);

    /**
     * Find assignment by order ID
     */
    List<DeliveryAssignment> findByOrderId(Long orderId);

    /**
     * Find assignment by catering request ID
     */
    List<DeliveryAssignment> findByCateringRequestId(Long cateringRequestId);

    // ==================== ACTIVE ASSIGNMENTS ====================

    /**
     * Find all active assignments (not delivered or cancelled)
     * Used for manager dashboard
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT')")
    List<DeliveryAssignment> findActiveAssignments();

    /**
     * Find active assignments for a specific driver
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.deliveryPersonnel.id = :personnelId AND d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT')")
    List<DeliveryAssignment> findActiveAssignmentsByPersonnel(@Param("personnelId") Long personnelId);

    // ==================== STATISTICS & COUNTS ====================

    /**
     * Count total deliveries completed by a driver
     */
    @Query("SELECT COUNT(d) FROM DeliveryAssignment d WHERE d.deliveryPersonnel.id = :personnelId AND d.status = 'DELIVERED'")
    Long countCompletedDeliveriesByPersonnel(@Param("personnelId") Long personnelId);

    /**
     * Count total deliveries for a driver (all statuses)
     */
    @Query("SELECT COUNT(d) FROM DeliveryAssignment d WHERE d.deliveryPersonnel.id = :personnelId")
    Long countTotalDeliveriesByPersonnel(@Param("personnelId") Long personnelId);

    /**
     * Count pending deliveries (assigned but not started)
     */
    @Query("SELECT COUNT(d) FROM DeliveryAssignment d WHERE d.status = 'ASSIGNED'")
    Long countPendingDeliveries();

    /**
     * Count deliveries in transit
     */
    @Query("SELECT COUNT(d) FROM DeliveryAssignment d WHERE d.status = 'IN_TRANSIT'")
    Long countInTransitDeliveries();

    /**
     * Count deliveries picked up today
     */
    @Query("SELECT COUNT(d) FROM DeliveryAssignment d WHERE d.status = 'PICKED_UP' AND DATE(d.pickedUpAt) = CURRENT_DATE")
    Long countPickedUpToday();

    // ==================== DATE RANGE QUERIES ====================

    /**
     * Find deliveries completed between dates (for reports)
     */
    List<DeliveryAssignment> findByDeliveredAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find today's deliveries for a driver
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.deliveryPersonnel.id = :personnelId AND DATE(d.assignedAt) = CURRENT_DATE")
    List<DeliveryAssignment> findTodayDeliveriesByPersonnel(@Param("personnelId") Long personnelId);

    /**
     * Find deliveries assigned on a specific date
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE DATE(d.assignedAt) = :date")
    List<DeliveryAssignment> findDeliveriesByAssignedDate(@Param("date") LocalDateTime date);

    /**
     * Find deliveries by status and date range
     */
    List<DeliveryAssignment> findByStatusAndAssignedAtBetween(String status, LocalDateTime start, LocalDateTime end);

    // ==================== TYPE-SPECIFIC QUERIES ====================

    /**
     * Find all order deliveries
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.assignmentType = 'ORDER_DELIVERY'")
    List<DeliveryAssignment> findAllOrderDeliveries();

    /**
     * Find all catering deliveries
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.assignmentType = 'CATERING_DELIVERY'")
    List<DeliveryAssignment> findAllCateringDeliveries();

    /**
     * Find active catering deliveries (for truck assignment)
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.assignmentType = 'CATERING_DELIVERY' AND d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT')")
    List<DeliveryAssignment> findActiveCateringDeliveries();

    // ==================== CUSTOMER-SPECIFIC ====================

    /**
     * Find deliveries for a customer by phone number
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.customerPhone = :phoneNumber ORDER BY d.assignedAt DESC")
    List<DeliveryAssignment> findDeliveriesByCustomerPhone(@Param("phoneNumber") String phoneNumber);

    /**
     * Find deliveries for a customer by name
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.customerName LIKE CONCAT('%', :name, '%') ORDER BY d.assignedAt DESC")
    List<DeliveryAssignment> findDeliveriesByCustomerName(@Param("name") String name);

    // ==================== UPDATE METHODS ====================

    /**
     * Update assignment status
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryAssignment d SET d.status = :status WHERE d.id = :id")
    void updateAssignmentStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * Update assignment status and set picked up time
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryAssignment d SET d.status = 'PICKED_UP', d.pickedUpAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void markAsPickedUp(@Param("id") Long id);

    /**
     * Update assignment status and set in transit time
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryAssignment d SET d.status = 'IN_TRANSIT', d.inTransitAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void markAsInTransit(@Param("id") Long id);

    /**
     * Update assignment status and set delivered time
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryAssignment d SET d.status = 'DELIVERED', d.deliveredAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void markAsDelivered(@Param("id") Long id);

    // ==================== REVENUE QUERIES ====================

    /**
     * Get total revenue from delivered orders
     */
    @Query("SELECT COALESCE(SUM(d.totalAmount), 0) FROM DeliveryAssignment d WHERE d.status = 'DELIVERED' AND d.assignmentType = 'ORDER_DELIVERY'")
    Double getTotalRevenueFromDeliveredOrders();

    /**
     * Get total revenue from delivered catering
     */
    @Query("SELECT COALESCE(SUM(d.totalAmount), 0) FROM DeliveryAssignment d WHERE d.status = 'DELIVERED' AND d.assignmentType = 'CATERING_DELIVERY'")
    Double getTotalRevenueFromDeliveredCatering();

    /**
     * Get total revenue from all delivered assignments
     */
    @Query("SELECT COALESCE(SUM(d.totalAmount), 0) FROM DeliveryAssignment d WHERE d.status = 'DELIVERED'")
    Double getTotalRevenueFromAllDeliveries();

    // ==================== MONITORING QUERIES ====================

    /**
     * Find assignments that have been in transit for more than 1 hour (for monitoring)
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.status = 'IN_TRANSIT' AND d.inTransitAt <= :threshold")
    List<DeliveryAssignment> findDelayedDeliveries(@Param("threshold") LocalDateTime threshold);

    /**
     * Get all assignments for a specific date range with status filter
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.assignedAt BETWEEN :startDate AND :endDate AND (:status IS NULL OR d.status = :status)")
    List<DeliveryAssignment> findAssignmentsByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate,
                                                                 @Param("status") String status);

    /**
     * Get assignments by driver name (partial match)
     */
    @Query("SELECT d FROM DeliveryAssignment d WHERE d.deliveryPersonnel.name LIKE CONCAT('%', :driverName, '%')")
    List<DeliveryAssignment> findByDriverNameContaining(@Param("driverName") String driverName);

    /**
     * Get all assignments with driver details (for reporting)
     */
    @Query("SELECT d FROM DeliveryAssignment d JOIN FETCH d.deliveryPersonnel ORDER BY d.assignedAt DESC")
    List<DeliveryAssignment> findAllWithDriverDetails();
}