package com.bakery.system.service;

import com.bakery.system.dto.InventoryReportDTO;
import com.bakery.system.dto.MonthlyStatsDTO;
import com.bakery.system.dto.MonthlySummaryDTO;
import com.bakery.system.entity.Product;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryReportService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryReportService.class);

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final OrderRepository orderRepository;

    public InventoryReportService(ProductRepository productRepository,
                                  StockMovementRepository stockMovementRepository,
                                  OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.orderRepository = orderRepository;
    }

    public List<InventoryReportDTO> generateInventoryReport(String category, String stockStatus,
                                                            LocalDate startDate, LocalDate endDate) {
        try {
            List<Product> products = productRepository.findAll();

            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
            LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

            return products.stream()
                    .filter(p -> {
                        if (category == null || category.isEmpty()) return true;
                        String productCategory = p.getCategory() != null ? p.getCategory().getName() : "Uncategorized";
                        return productCategory.equals(category);
                    })
                    .filter(p -> {
                        if (stockStatus == null || stockStatus.isEmpty()) return true;
                        Integer stock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                        if ("LOW".equals(stockStatus)) return stock < 5;
                        if ("NORMAL".equals(stockStatus)) return stock >= 5;
                        if ("OUT".equals(stockStatus)) return stock == 0;
                        return true;
                    })
                    .map(p -> {
                        InventoryReportDTO dto = new InventoryReportDTO();
                        dto.setProductId(p.getId());
                        dto.setProductName(p.getName());
                        dto.setCategory(p.getCategory() != null ? p.getCategory().getName() : "Uncategorized");
                        dto.setCurrentStock(p.getStockQuantity() != null ? p.getStockQuantity() : 0);
                        dto.setMinStockThreshold(5);
                        dto.setUnitPrice(p.getPrice());
                        dto.setExpirationDate(p.getExpirationDate());
                        dto.setStatus(p.getStatus() != null ? p.getStatus() : "Inactive");

                        Integer totalSold = stockMovementRepository.getTotalSoldForProduct(p.getId(), start, end);
                        Integer totalRestocked = stockMovementRepository.getTotalRestockedForProduct(p.getId(), start, end);

                        dto.setTotalSold(totalSold != null ? totalSold : 0);
                        dto.setTotalRestocked(totalRestocked != null ? totalRestocked : 0);
                        dto.setTotalRevenue((totalSold != null ? totalSold : 0) * p.getPrice());

                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error generating inventory report: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getInventorySummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            List<Product> allProducts = productRepository.findAll();

            long totalProducts = allProducts.size();
            long lowStockCount = allProducts.stream()
                    .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() < 5)
                    .count();
            long outOfStockCount = allProducts.stream()
                    .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() == 0)
                    .count();

            double totalInventoryValue = allProducts.stream()
                    .mapToDouble(p -> (p.getStockQuantity() != null ? p.getStockQuantity() : 0) * p.getPrice())
                    .sum();

            long expiringCount = 0;
            try {
                LocalDate today = LocalDate.now();
                LocalDate threshold = today.plusDays(7);
                expiringCount = productRepository.findByExpirationDateBetween(today, threshold).size();
            } catch (Exception e) {
                expiringCount = 0;
            }

            Map<String, Long> categoryBreakdown = allProducts.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                            Collectors.counting()
                    ));

            summary.put("totalProducts", totalProducts);
            summary.put("lowStockCount", lowStockCount);
            summary.put("outOfStockCount", outOfStockCount);
            summary.put("totalInventoryValue", totalInventoryValue);
            summary.put("expiringCount", expiringCount);
            summary.put("categoryBreakdown", categoryBreakdown);

        } catch (Exception e) {
            logger.error("Error calculating inventory summary: {}", e.getMessage(), e);
            summary.put("totalProducts", 0);
            summary.put("lowStockCount", 0);
            summary.put("outOfStockCount", 0);
            summary.put("totalInventoryValue", 0.0);
            summary.put("expiringCount", 0);
            summary.put("categoryBreakdown", new HashMap<>());
        }

        return summary;
    }

    public List<MonthlyStatsDTO> getMonthlyStats() {
        List<MonthlyStatsDTO> monthlyStats = new ArrayList<>();

        try {
            LocalDateTime oneYearAgo = LocalDateTime.now().minusMonths(12);
            List<MonthlySummaryDTO> results = stockMovementRepository.getMonthlySummary(oneYearAgo);

            for (MonthlySummaryDTO summary : results) {
                YearMonth yearMonth = summary.getYearMonth();

                LocalDateTime monthStart = LocalDateTime.of(summary.getYear(), summary.getMonth(), 1, 0, 0);
                LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

                Long totalOrders = 0L;
                try {
                    totalOrders = orderRepository.countOrdersBetweenDates(monthStart, monthEnd);
                } catch (Exception e) {
                    totalOrders = 0L;
                }

                Double totalRevenue = 0.0;
                try {
                    totalRevenue = orderRepository.getTotalRevenueBetweenDates(monthStart, monthEnd);
                    if (totalRevenue == null) totalRevenue = 0.0;
                } catch (Exception e) {
                    totalRevenue = 0.0;
                }

                monthlyStats.add(new MonthlyStatsDTO(
                        yearMonth,
                        summary.getTotalSold() != null ? summary.getTotalSold().intValue() : 0,
                        totalRevenue,
                        summary.getTotalRestocked() != null ? summary.getTotalRestocked().intValue() : 0,
                        totalOrders != null ? totalOrders.intValue() : 0
                ));
            }

            monthlyStats.sort((a, b) -> b.getMonth().compareTo(a.getMonth()));

        } catch (Exception e) {
            logger.error("Error getting monthly stats: {}", e.getMessage(), e);
        }

        return monthlyStats;
    }

    public Map<String, Object> getCurrentMonthStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime now = LocalDateTime.now();

            Integer totalSold = stockMovementRepository.getTotalSoldAllProducts(monthStart, now);
            Integer totalRestocked = stockMovementRepository.getTotalRestockedAllProducts(monthStart, now);
            Double totalRevenue = orderRepository.getTotalRevenueBetweenDates(monthStart, now);
            Long totalOrders = orderRepository.countOrdersBetweenDates(monthStart, now);

            stats.put("totalSold", totalSold != null ? totalSold : 0);
            stats.put("totalRestocked", totalRestocked != null ? totalRestocked : 0);
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
            stats.put("totalOrders", totalOrders != null ? totalOrders : 0);

        } catch (Exception e) {
            logger.error("Error getting current month stats: {}", e.getMessage(), e);
            stats.put("totalSold", 0);
            stats.put("totalRestocked", 0);
            stats.put("totalRevenue", 0.0);
            stats.put("totalOrders", 0);
        }

        return stats;
    }

    public Map<String, Object> getTopSellingProducts(int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime now = LocalDateTime.now();

            List<Product> allProducts = productRepository.findAll();

            List<Map<String, Object>> topProducts = allProducts.stream()
                    .map(p -> {
                        Integer sold = stockMovementRepository.getTotalSoldForProduct(p.getId(), monthStart, now);
                        Map<String, Object> productData = new HashMap<>();
                        productData.put("name", p.getName());
                        productData.put("sold", sold != null ? sold : 0);
                        productData.put("revenue", (sold != null ? sold : 0) * p.getPrice());
                        return productData;
                    })
                    .filter(p -> (Integer) p.get("sold") > 0)
                    .sorted((a, b) -> ((Integer) b.get("sold")).compareTo((Integer) a.get("sold")))
                    .limit(limit)
                    .collect(Collectors.toList());

            result.put("topSellingProducts", topProducts);

        } catch (Exception e) {
            logger.error("Error getting top selling products: {}", e.getMessage(), e);
            result.put("topSellingProducts", new ArrayList<>());
        }

        return result;
    }
}