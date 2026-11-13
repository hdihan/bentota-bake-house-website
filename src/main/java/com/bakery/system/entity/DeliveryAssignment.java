package com.bakery.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_assignments")
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_personnel_id", nullable = false)
    private DeliveryPersonnel deliveryPersonnel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catering_request_id")
    private CateringRequest cateringRequest;

    @Column(nullable = false)
    private String assignmentType; // ORDER_DELIVERY, CATERING_DELIVERY

    private String pickupAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    private Double distance; // in km

    @Column(nullable = false)
    private String status; // ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime pickedUpAt;

    private LocalDateTime inTransitAt;

    private LocalDateTime deliveredAt;

    @Column(length = 500)
    private String notes;

    private String customerName;

    private String customerPhone;

    private Double totalAmount;

    // Default Constructor
    public DeliveryAssignment() {
        this.status = "ASSIGNED";
        this.assignedAt = LocalDateTime.now();
    }

    // Parameterized Constructor
    public DeliveryAssignment(DeliveryPersonnel deliveryPersonnel, String assignmentType,
                              String deliveryAddress, String customerName, String customerPhone) {
        this.deliveryPersonnel = deliveryPersonnel;
        this.assignmentType = assignmentType;
        this.deliveryAddress = deliveryAddress;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = "ASSIGNED";
        this.assignedAt = LocalDateTime.now();
    }

    // Pre-update method to auto-set timestamps based on status
    @PreUpdate
    protected void onUpdate() {
        if ("PICKED_UP".equals(this.status) && this.pickedUpAt == null) {
            this.pickedUpAt = LocalDateTime.now();
        }
        if ("IN_TRANSIT".equals(this.status) && this.inTransitAt == null) {
            this.inTransitAt = LocalDateTime.now();
        }
        if ("DELIVERED".equals(this.status) && this.deliveredAt == null) {
            this.deliveredAt = LocalDateTime.now();
        }
    }

    // ==================== GETTERS AND SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeliveryPersonnel getDeliveryPersonnel() {
        return deliveryPersonnel;
    }

    public void setDeliveryPersonnel(DeliveryPersonnel deliveryPersonnel) {
        this.deliveryPersonnel = deliveryPersonnel;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public CateringRequest getCateringRequest() {
        return cateringRequest;
    }

    public void setCateringRequest(CateringRequest cateringRequest) {
        this.cateringRequest = cateringRequest;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    public void setPickedUpAt(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }

    public LocalDateTime getInTransitAt() {
        return inTransitAt;
    }

    public void setInTransitAt(LocalDateTime inTransitAt) {
        this.inTransitAt = inTransitAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if delivery is completed
     */
    public boolean isCompleted() {
        return "DELIVERED".equals(this.status);
    }

    /**
     * Check if delivery is in progress
     */
    public boolean isInProgress() {
        return "ASSIGNED".equals(this.status) || "PICKED_UP".equals(this.status) || "IN_TRANSIT".equals(this.status);
    }

    /**
     * Check if delivery is cancelled
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(this.status);
    }

    /**
     * Get formatted status for display
     */
    public String getFormattedStatus() {
        switch (this.status) {
            case "ASSIGNED":
                return "📋 Assigned";
            case "PICKED_UP":
                return "📦 Picked Up";
            case "IN_TRANSIT":
                return "🚚 In Transit";
            case "DELIVERED":
                return "✅ Delivered";
            case "CANCELLED":
                return "❌ Cancelled";
            default:
                return this.status;
        }
    }

    /**
     * Get status badge color class for HTML
     */
    public String getStatusBadgeClass() {
        switch (this.status) {
            case "ASSIGNED":
                return "status-assigned";
            case "PICKED_UP":
                return "status-picked-up";
            case "IN_TRANSIT":
                return "status-in-transit";
            case "DELIVERED":
                return "status-delivered";
            case "CANCELLED":
                return "status-cancelled";
            default:
                return "status-default";
        }
    }

    /**
     * Get assignment type display name
     */
    public String getAssignmentTypeDisplay() {
        if ("ORDER_DELIVERY".equals(this.assignmentType)) {
            return "🛒 Order Delivery";
        } else if ("CATERING_DELIVERY".equals(this.assignmentType)) {
            return "🎉 Catering Delivery";
        }
        return this.assignmentType;
    }

    /**
     * Calculate delivery duration in minutes (from assigned to delivered)
     */
    public Long getDeliveryDurationMinutes() {
        if (this.assignedAt != null && this.deliveredAt != null) {
            return java.time.Duration.between(this.assignedAt, this.deliveredAt).toMinutes();
        }
        return null;
    }

    /**
     * Calculate transit time in minutes (from picked up to delivered)
     */
    public Long getTransitTimeMinutes() {
        if (this.pickedUpAt != null && this.deliveredAt != null) {
            return java.time.Duration.between(this.pickedUpAt, this.deliveredAt).toMinutes();
        }
        return null;
    }

    @Override
    public String toString() {
        return "DeliveryAssignment{" +
                "id=" + id +
                ", assignmentType='" + assignmentType + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", status='" + status + '\'' +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}