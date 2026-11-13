package com.bakery.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name too long")
    private String name;

    @Size(max = 500, message = "Description too long")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    // Now ManyToOne relationship with Category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String status;

    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private String imageUrl;
    private Double averageRating; // average rating (0-5)

    // Constructors
    public Product() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }



    // Getters and setters
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    private LocalDate expirationDate;

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
}