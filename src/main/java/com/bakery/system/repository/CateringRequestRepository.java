package com.bakery.system.repository;

import com.bakery.system.entity.CateringRequest;
import com.bakery.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CateringRequestRepository extends JpaRepository<CateringRequest, Long> {

    // ========== EXISTING METHODS ==========

    List<CateringRequest> findByCustomer(User customer);

    List<CateringRequest> findByStatus(String status);

    boolean existsByCustomer(User customer);

    List<CateringRequest> findByCustomerAndStatus(User customer, String status);

    // ========== NEW METHODS FOR BETTER PERFORMANCE ==========

    /**
     * Count catering requests by status (uses database query)
     * Example: long count = cateringRequestRepository.countByStatus("PENDING");
     *
     * @param status The catering status (PENDING, QUOTED, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED)
     * @return number of catering requests with the given status
     */
    long countByStatus(String status);

    /**
     * Count catering requests by multiple statuses
     * Example: long count = cateringRequestRepository.countByStatusIn(List.of("PENDING", "QUOTED", "CONFIRMED"));
     *
     * @param statuses List of statuses to count
     * @return number of catering requests with status in the given list
     */
    long countByStatusIn(List<String> statuses);

    /**
     * Find catering requests by multiple statuses
     *
     * @param statuses List of statuses
     * @return List of catering requests with status in the given list
     */
    List<CateringRequest> findByStatusIn(List<String> statuses);

    /**
     * Count active catering requests (PENDING, QUOTED, CONFIRMED, PREPARING)
     *
     * @return number of active catering requests
     */
    @Query("SELECT COUNT(c) FROM CateringRequest c WHERE c.status IN ('PENDING', 'QUOTED', 'CONFIRMED', 'PREPARING')")
    long countActiveRequests();

    /**
     * Count catering requests by customer and status
     *
     * @param customer The customer
     * @param status The status
     * @return number of requests for the customer with given status
     */
    long countByCustomerAndStatus(User customer, String status);

    /**
     * Find catering requests by event date (upcoming events)
     *
     * @param fromDate Start date
     * @param toDate End date
     * @return List of catering requests between dates
     */
    List<CateringRequest> findByEventDateBetween(LocalDate fromDate, LocalDate toDate);

    /**
     * Find catering requests by date range and status
     *
     * @param startDate Start date
     * @param endDate End date
     * @param status Status
     * @return List of catering requests
     */
    List<CateringRequest> findByEventDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, String status);

    /**
     * Get upcoming events (today and future)
     *
     * @param today Today's date
     * @return List of upcoming catering requests
     */
    List<CateringRequest> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate today);

    /**
     * Get catering requests created in last 7 days
     *
     * @param dateThreshold Date threshold (7 days ago)
     * @return List of recent catering requests
     */
    List<CateringRequest> findByCreatedAtAfter(LocalDateTime dateThreshold);

    /**
     * Count catering requests by customer
     *
     * @param customer The customer
     * @return total number of requests for the customer
     */
    long countByCustomer(User customer);

    /**
     * Get total revenue from confirmed catering requests (DELIVERED or COMPLETED)
     *
     * @return total revenue from completed catering
     */
    @Query("SELECT COALESCE(SUM(c.totalQuotedPrice), 0) FROM CateringRequest c WHERE c.status IN ('DELIVERED', 'COMPLETED')")
    Double getTotalRevenueFromCompletedCatering();

    /**
     * Get today's new catering requests count
     *
     * @return number of catering requests created today
     */
    @Query("SELECT COUNT(c) FROM CateringRequest c WHERE DATE(c.createdAt) = CURRENT_DATE")
    long countTodayRequests();

    /**
     * Get catering requests that need attention (pending for more than 3 days)
     *
     * @param threshold Date threshold (3 days ago)
     * @return List of stale catering requests
     */
    @Query("SELECT c FROM CateringRequest c WHERE c.status = 'PENDING' AND c.createdAt < :threshold")
    List<CateringRequest> findStalePendingRequests(@Param("threshold") LocalDateTime threshold);

    /**
     * Get confirmed catering requests for upcoming week
     *
     * @param today Today's date
     * @param nextWeek Next week's date
     * @return List of confirmed requests for next 7 days
     */
    List<CateringRequest> findByStatusAndEventDateBetween(String status, LocalDate today, LocalDate nextWeek);
}