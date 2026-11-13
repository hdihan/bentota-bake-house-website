package com.bakery.system.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "set_menus")
public class SetMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Menu name is required")
    @Size(max = 100, message = "Name too long")
    private String name;

    @Size(max = 500, message = "Description too long")
    private String description;

    @NotBlank(message = "Category is required")
    private String category; // BREAKFAST, LUNCH, DINNER, BITES

    @NotBlank(message = "Status is required")
    private String status; // ACTIVE, INACTIVE

    @PositiveOrZero(message = "Base price cannot be negative")
    private BigDecimal basePrice;

    @OneToMany(mappedBy = "setMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private List<SetMenuItem> items = new ArrayList<>();

    public SetMenu() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public List<SetMenuItem> getItems() { return items; }
    public void setItems(List<SetMenuItem> items) { this.items = items; }
}