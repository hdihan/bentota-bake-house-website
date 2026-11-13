package com.bakery.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String movementType; // IN, OUT, ADJUSTMENT

    @Column(nullable = false)
    private Integer quantity;

    private String reason;

    @Column(nullable = false)
    private LocalDateTime movementDate;

    private String performedBy;

    public StockMovement() {
        this.movementDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        movementDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDateTime movementDate) { this.movementDate = movementDate; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}