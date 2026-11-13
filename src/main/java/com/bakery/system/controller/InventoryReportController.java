package com.bakery.system.controller;

import com.bakery.system.dto.InventoryReportDTO;
import com.bakery.system.dto.MonthlyStatsDTO;
import com.bakery.system.repository.CategoryRepository;
import com.bakery.system.service.InventoryReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class InventoryReportController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryReportController.class);

    private final InventoryReportService reportService;
    private final CategoryRepository categoryRepository;

    public InventoryReportController(InventoryReportService reportService,
                                     CategoryRepository categoryRepository) {
        this.reportService = reportService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/inventory")
    public String inventoryReportPage(Model model) {
        try {
            logger.info("Loading inventory report page");

            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("summary", reportService.getInventorySummary());
            model.addAttribute("currentMonthStats", reportService.getCurrentMonthStats());
            model.addAttribute("monthlyStats", reportService.getMonthlyStats());
            model.addAttribute("topSelling", reportService.getTopSellingProducts(5));

            logger.info("Inventory report page loaded successfully");
            return "reports/inventory-report";

        } catch (Exception e) {
            logger.error("Error loading inventory report page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load report: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/inventory/data")
    @ResponseBody
    public List<InventoryReportDTO> getInventoryReportData(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            logger.info("Fetching inventory report data");
            return reportService.generateInventoryReport(category, stockStatus, startDate, endDate);
        } catch (Exception e) {
            logger.error("Error fetching inventory report data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @GetMapping("/summary")
    @ResponseBody
    public Map<String, Object> getSummary() {
        try {
            return reportService.getInventorySummary();
        } catch (Exception e) {
            logger.error("Error fetching summary: {}", e.getMessage(), e);
            Map<String, Object> errorSummary = new HashMap<>();
            errorSummary.put("totalProducts", 0);
            errorSummary.put("lowStockCount", 0);
            errorSummary.put("outOfStockCount", 0);
            errorSummary.put("totalInventoryValue", 0.0);
            errorSummary.put("expiringCount", 0);
            errorSummary.put("categoryBreakdown", new HashMap<>());
            return errorSummary;
        }
    }

    @GetMapping("/monthly-stats")
    @ResponseBody
    public List<MonthlyStatsDTO> getMonthlyStats() {
        return reportService.getMonthlyStats();
    }

    @GetMapping("/current-month")
    @ResponseBody
    public Map<String, Object> getCurrentMonthStats() {
        return reportService.getCurrentMonthStats();
    }

    @GetMapping("/top-selling")
    @ResponseBody
    public Map<String, Object> getTopSelling(@RequestParam(defaultValue = "5") int limit) {
        return reportService.getTopSellingProducts(limit);
    }
}