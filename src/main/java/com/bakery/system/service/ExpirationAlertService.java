package com.bakery.system.service;

import com.bakery.system.entity.Product;
import com.bakery.system.entity.InventoryItem;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.InventoryItemRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpirationAlertService {

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final EmailService emailService;

    public ExpirationAlertService(ProductRepository productRepository,
                                  InventoryItemRepository inventoryItemRepository,
                                  EmailService emailService) {
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * *") // every day at 9:00 AM
    public void sendExpirationAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(7);

        List<Product> expiringProducts = productRepository.findByExpirationDateBetween(today, threshold);
        List<InventoryItem> expiringInventory = inventoryItemRepository.findByExpirationDateBetween(today, threshold);

        if (!expiringProducts.isEmpty() || !expiringInventory.isEmpty()) {
            emailService.sendExpirationAlert(expiringProducts, expiringInventory);
        }
    }
}