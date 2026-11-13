package com.bakery.system.repository;

import com.bakery.system.entity.DeliveryPersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPersonnelRepository extends JpaRepository<DeliveryPersonnel, Long> {

    // ==================== BASIC FIND METHODS ====================

    /**
     * Find delivery personnel by status
     */
    List<DeliveryPersonnel> findByStatus(String status);

    /**
     * Find delivery personnel by email (used for driver login)
     */
    Optional<DeliveryPersonnel> findByEmail(String email);

    /**
     * Find delivery personnel by phone number
     */
    Optional<DeliveryPersonnel> findByPhone(String phone);

    /**
     * Find delivery personnel by name (contains - case insensitive)
     */
    List<DeliveryPersonnel> findByNameContainingIgnoreCase(String name);

    /**
     * Find delivery personnel by vehicle type
     */
    List<DeliveryPersonnel> findByVehicleType(String vehicleType);

    // ==================== AVAILABILITY QUERIES ====================

    /**
     * Find available personnel sorted by workload (least deliveries first)
     */
    @Query("SELECT d FROM DeliveryPersonnel d WHERE d.status = 'AVAILABLE' ORDER BY d.completedDeliveries ASC")
    List<DeliveryPersonnel> findAvailableSortedByWorkload();

    /**
     * Find available personnel with specific vehicle type
     */
    @Query("SELECT d FROM DeliveryPersonnel d WHERE d.status = 'AVAILABLE' AND d.vehicleType = :vehicleType")
    List<DeliveryPersonnel> findAvailableByVehicleType(@Param("vehicleType") String vehicleType);

    /**
     * Find available personnel who have a truck (for large catering orders)
     */
    @Query("SELECT d FROM DeliveryPersonnel d WHERE d.status = 'AVAILABLE' AND d.vehicleType = 'Truck'")
    List<DeliveryPersonnel> findAvailableTruckDrivers();

    /**
     * Count busy drivers
     */
    @Query("SELECT COUNT(d) FROM DeliveryPersonnel d WHERE d.status = 'BUSY'")
    Long countBusyDrivers();

    /**
     * Count available drivers
     */
    @Query("SELECT COUNT(d) FROM DeliveryPersonnel d WHERE d.status = 'AVAILABLE'")
    Long countAvailableDrivers();

    // ==================== EXISTENCE CHECKS ====================

    /**
     * Check if phone number already exists
     */
    boolean existsByPhone(String phone);

    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if driver exists and is available
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DeliveryPersonnel d WHERE d.id = :id AND d.status = 'AVAILABLE'")
    boolean isDriverAvailable(@Param("id") Long id);

    // ==================== UPDATE METHODS ====================

    /**
     * Update driver status
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryPersonnel d SET d.status = :status WHERE d.id = :id")
    void updateDriverStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * Increment completed deliveries count for a driver
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryPersonnel d SET d.completedDeliveries = d.completedDeliveries + 1 WHERE d.id = :id")
    void incrementCompletedDeliveries(@Param("id") Long id);

    /**
     * Update driver rating
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeliveryPersonnel d SET d.rating = :rating WHERE d.id = :id")
    void updateDriverRating(@Param("id") Long id, @Param("rating") Double rating);

    // ==================== STATISTICS QUERIES ====================

    /**
     * Get average rating of all drivers
     */
    @Query("SELECT AVG(d.rating) FROM DeliveryPersonnel d")
    Double getAverageDriverRating();

    /**
     * Get total deliveries completed by all drivers
     */
    @Query("SELECT SUM(d.completedDeliveries) FROM DeliveryPersonnel d")
    Long getTotalDeliveriesCompleted();

    /**
     * Get drivers with highest completed deliveries (top performers)
     */
    @Query("SELECT d FROM DeliveryPersonnel d ORDER BY d.completedDeliveries DESC")
    List<DeliveryPersonnel> findTopPerformers();

    /**
     * Get drivers with rating above given value
     */
    List<DeliveryPersonnel> findByRatingGreaterThanEqual(Double rating);

    // ==================== COMPLEX QUERIES ====================

    /**
     * Find drivers by status and vehicle type
     */
    List<DeliveryPersonnel> findByStatusAndVehicleType(String status, String vehicleType);

    /**
     * Find drivers who joined after a certain date
     */
    List<DeliveryPersonnel> findByJoinedDateAfter(LocalDateTime date);

    /**
     * Get all drivers with their assignment count
     */
    @Query("SELECT d, SIZE(d.assignments) FROM DeliveryPersonnel d")
    List<Object[]> findAllDriversWithAssignmentCount();
}