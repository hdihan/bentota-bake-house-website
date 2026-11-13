package com.bakery.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "set_menu_items")
public class SetMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "set_menu_id", nullable = false)
    private SetMenu setMenu;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String itemName;
    private String description;

    @Positive(message = "Default quantity must be at least 1")
    private Integer defaultQuantity;

    public SetMenuItem() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SetMenu getSetMenu() { return setMenu; }
    public void setSetMenu(SetMenu setMenu) { this.setMenu = setMenu; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDefaultQuantity() { return defaultQuantity; }
    public void setDefaultQuantity(Integer defaultQuantity) { this.defaultQuantity = defaultQuantity; }
}