package com.bakery.system.dto;

import java.time.YearMonth;

public class MonthlySummaryDTO {
    private Integer year;
    private Integer month;
    private Long totalSold;
    private Long totalRestocked;

    public MonthlySummaryDTO() {}

    public MonthlySummaryDTO(Integer year, Integer month, Long totalSold, Long totalRestocked) {
        this.year = year;
        this.month = month;
        this.totalSold = totalSold;
        this.totalRestocked = totalRestocked;
    }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Long getTotalSold() { return totalSold; }
    public void setTotalSold(Long totalSold) { this.totalSold = totalSold; }

    public Long getTotalRestocked() { return totalRestocked; }
    public void setTotalRestocked(Long totalRestocked) { this.totalRestocked = totalRestocked; }

    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }
}