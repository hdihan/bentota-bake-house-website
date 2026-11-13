package com.bakery.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "catering_requests")
public class CateringRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @NotNull(message = "Event date is required")
    // Comment out or remove the @Future validation to allow past dates for existing records
    // @Future(message = "Event date must be in the future")
    private LocalDate eventDate;

    @NotNull(message = "Event time is required")
    private LocalTime eventTime;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location too long")
    private String location;

    @NotNull(message = "Number of guests is required")
    @Positive(message = "Number of guests must be at least 1")
    private Integer numberOfGuests;

    @Size(max = 1000, message = "Dietary restrictions too long")
    private String dietaryRestrictions;

    @Size(max = 1000, message = "Special requirements too long")
    private String specialRequirements;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PositiveOrZero(message = "Quoted price cannot be negative")
    private BigDecimal totalQuotedPrice;

    private String managerNotes;
    private LocalDateTime quotationDate;
    private LocalDateTime confirmationDate;

    private Double distance;
    private Double deliveryFee;
    private BigDecimal serviceCharge;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CateringRequestItem> items = new ArrayList<>();

    public CateringRequest() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }
    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }
    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public BigDecimal getTotalQuotedPrice() { return totalQuotedPrice; }
    public void setTotalQuotedPrice(BigDecimal totalQuotedPrice) { this.totalQuotedPrice = totalQuotedPrice; }
    public String getManagerNotes() { return managerNotes; }
    public void setManagerNotes(String managerNotes) { this.managerNotes = managerNotes; }
    public LocalDateTime getQuotationDate() { return quotationDate; }
    public void setQuotationDate(LocalDateTime quotationDate) { this.quotationDate = quotationDate; }
    public LocalDateTime getConfirmationDate() { return confirmationDate; }
    public void setConfirmationDate(LocalDateTime confirmationDate) { this.confirmationDate = confirmationDate; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }
    public List<CateringRequestItem> getItems() { return items; }
    public void setItems(List<CateringRequestItem> items) { this.items = items; }
}