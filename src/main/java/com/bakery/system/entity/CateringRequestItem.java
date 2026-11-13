package com.bakery.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Entity
@Table(name = "catering_request_items")
public class CateringRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private CateringRequest request;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "set_menu_id")
    private SetMenu setMenu;

    private String itemName;
    private String description;

    @Positive(message = "Quantity must be at least 1")
    private Integer quantity;

    private String notes;
    private String modificationRequest;

    @PositiveOrZero(message = "Unit price cannot be negative")
    private BigDecimal quotedUnitPrice;

    public CateringRequestItem() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CateringRequest getRequest() { return request; }
    public void setRequest(CateringRequest request) { this.request = request; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public SetMenu getSetMenu() { return setMenu; }
    public void setSetMenu(SetMenu setMenu) { this.setMenu = setMenu; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getModificationRequest() { return modificationRequest; }
    public void setModificationRequest(String modificationRequest) { this.modificationRequest = modificationRequest; }
    public BigDecimal getQuotedUnitPrice() { return quotedUnitPrice; }
    public void setQuotedUnitPrice(BigDecimal quotedUnitPrice) { this.quotedUnitPrice = quotedUnitPrice; }
}