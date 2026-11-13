package com.bakery.system.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryReportDTO {
    private Long productId;
    private String productName;
    private String category;
    private Integer currentStock;
    private Integer minStockThreshold;
    private Double unitPrice;
    private LocalDate expirationDate;
    private String status;
    private Integer totalSold;
    private Double totalRevenue;
    private Integer totalRestocked;
    private LocalDateTime lastUpdated;

    // Constructors
    public InventoryReportDTO() {}

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getMinStockThreshold() { return minStockThreshold; }
    public void setMinStockThreshold(Integer minStockThreshold) { this.minStockThreshold = minStockThreshold; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalSold() { return totalSold; }
    public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Integer getTotalRestocked() { return totalRestocked; }
    public void setTotalRestocked(Integer totalRestocked) { this.totalRestocked = totalRestocked; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}