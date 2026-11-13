package com.bakery.system.repository;

import com.bakery.system.entity.DeliverySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliverySettingsRepository extends JpaRepository<DeliverySettings, Long> {

    /**
     * Find all active delivery settings
     * Returns list of active settings
     */
    @Query("SELECT d FROM DeliverySettings d WHERE d.active = true")
    List<DeliverySettings> findAllActive();

    /**
     * Find the currently active delivery settings
     * Returns the first active setting or null if none exists
     */
    default DeliverySettings findActive() {
        List<DeliverySettings> active = findAllActive();
        return active.isEmpty() ? null : active.get(0);
    }

    /**
     * Find active settings as Optional
     */
    default Optional<DeliverySettings> findActiveOptional() {
        return Optional.ofNullable(findActive());
    }

    /**
     * Check if any active settings exist
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DeliverySettings d WHERE d.active = true")
    boolean hasActiveSettings();

    /**
     * Deactivate all settings
     */
    @Query("UPDATE DeliverySettings d SET d.active = false")
    void deactivateAll();
}