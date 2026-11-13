package com.bakery.system.dto;

import java.time.YearMonth;

public class MonthlyStatsDTO {
    private YearMonth month;
    private Integer totalSold;
    private Double totalRevenue;
    private Integer totalRestocked;
    private Integer totalOrders;

    public MonthlyStatsDTO() {}

    public MonthlyStatsDTO(YearMonth month, Integer totalSold, Double totalRevenue,
                           Integer totalRestocked, Integer totalOrders) {
        this.month = month;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
        this.totalRestocked = totalRestocked;
        this.totalOrders = totalOrders;
    }

    public YearMonth getMonth() { return month; }
    public void setMonth(YearMonth month) { this.month = month; }

    public Integer getTotalSold() { return totalSold; }
    public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Integer getTotalRestocked() { return totalRestocked; }
    public void setTotalRestocked(Integer totalRestocked) { this.totalRestocked = totalRestocked; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
}