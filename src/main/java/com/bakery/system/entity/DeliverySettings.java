package com.bakery.system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_settings")
public class DeliverySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "free_distance_km")
    private Double freeDistanceKm;

    @Column(name = "additional_charge")
    private Double additionalCharge;

    private Boolean active;

    // Default Constructor
    public DeliverySettings() {
        this.freeDistanceKm = 5.0;
        this.additionalCharge = 500.0;
        this.active = true;
    }

    // Parameterized Constructor
    public DeliverySettings(Double freeDistanceKm, Double additionalCharge, Boolean active) {
        this.freeDistanceKm = freeDistanceKm;
        this.additionalCharge = additionalCharge;
        this.active = active;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getFreeDistanceKm() {
        return freeDistanceKm;
    }

    public void setFreeDistanceKm(Double freeDistanceKm) {
        this.freeDistanceKm = freeDistanceKm;
    }

    public Double getAdditionalCharge() {
        return additionalCharge;
    }

    public void setAdditionalCharge(Double additionalCharge) {
        this.additionalCharge = additionalCharge;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "DeliverySettings{" +
                "id=" + id +
                ", freeDistanceKm=" + freeDistanceKm +
                ", additionalCharge=" + additionalCharge +
                ", active=" + active +
                '}';
    }
}