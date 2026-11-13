package com.bakery.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_personnel")
public class DeliveryPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    @Column(nullable = false, unique = true)
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address (e.g., name@example.com)")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType; // Bike, Car, Van, Truck

    private String vehicleNumber;

    @NotBlank(message = "Status is required")
    private String status; // AVAILABLE, BUSY, OFFLINE

    private Double currentLatitude;
    private Double currentLongitude;
    private Integer completedDeliveries;
    private Double rating;
    private LocalDateTime joinedDate;

    @OneToMany(mappedBy = "deliveryPersonnel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryAssignment> assignments = new ArrayList<>();

    // Default Constructor
    public DeliveryPersonnel() {
        this.status = "AVAILABLE";
        this.completedDeliveries = 0;
        this.rating = 5.0;
        this.joinedDate = LocalDateTime.now();
    }

    // Parameterized Constructor
    public DeliveryPersonnel(String name, String phone, String email, String vehicleType, String vehicleNumber) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.status = "AVAILABLE";
        this.completedDeliveries = 0;
        this.rating = 5.0;
        this.joinedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Integer getCompletedDeliveries() {
        return completedDeliveries;
    }

    public void setCompletedDeliveries(Integer completedDeliveries) {
        this.completedDeliveries = completedDeliveries;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }

    public List<DeliveryAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<DeliveryAssignment> assignments) {
        this.assignments = assignments;
    }

    // Helper Methods
    public void incrementCompletedDeliveries() {
        this.completedDeliveries++;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(this.status);
    }

    public boolean isBusy() {
        return "BUSY".equals(this.status);
    }

    public boolean hasTruck() {
        return "Truck".equals(this.vehicleType);
    }

    @Override
    public String toString() {
        return "DeliveryPersonnel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", status='" + status + '\'' +
                ", completedDeliveries=" + completedDeliveries +
                '}';
    }
}