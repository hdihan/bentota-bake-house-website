package com.bakery.system.controller;

import com.bakery.system.entity.DeliverySettings;
import com.bakery.system.repository.DeliverySettingsRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff/delivery-settings")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
public class DeliverySettingsController {

    private final DeliverySettingsRepository deliverySettingsRepository;

    public DeliverySettingsController(DeliverySettingsRepository deliverySettingsRepository) {
        this.deliverySettingsRepository = deliverySettingsRepository;
    }

    @GetMapping
    public String showSettings(Model model, RedirectAttributes redirectAttributes) {
        try {
            DeliverySettings settings = deliverySettingsRepository.findActive();
            if (settings == null) {
                settings = new DeliverySettings();
                settings.setFreeDistanceKm(5.0);
                settings.setAdditionalCharge(500.0);
                settings.setActive(true);
            }
            model.addAttribute("settings", settings);
            return "delivery-settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading settings: " + e.getMessage());
            return "redirect:/staff/delivery/dashboard";
        }
    }

    @PostMapping("/update")
    public String updateSettings(@ModelAttribute DeliverySettings settings,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Deactivate all existing settings
            List<DeliverySettings> allSettings = deliverySettingsRepository.findAll();
            for (DeliverySettings existing : allSettings) {
                existing.setActive(false);
                deliverySettingsRepository.save(existing);
            }

            // Save the new active settings
            settings.setActive(true);
            deliverySettingsRepository.save(settings);

            // Professional success notification
            redirectAttributes.addFlashAttribute("success", "✅ Delivery settings updated successfully! New settings: Free distance " +
                    settings.getFreeDistanceKm() + "km, Additional charge Rs. " + settings.getAdditionalCharge());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Failed to update settings: " + e.getMessage());
        }
        return "redirect:/staff/delivery-settings";
    }

    // Additional endpoint to reset to default settings
    @PostMapping("/reset")
    public String resetSettings(RedirectAttributes redirectAttributes) {
        try {
            // Deactivate all existing settings
            List<DeliverySettings> allSettings = deliverySettingsRepository.findAll();
            for (DeliverySettings existing : allSettings) {
                existing.setActive(false);
                deliverySettingsRepository.save(existing);
            }

            // Create and save default settings
            DeliverySettings defaultSettings = new DeliverySettings();
            defaultSettings.setFreeDistanceKm(5.0);
            defaultSettings.setAdditionalCharge(500.0);
            defaultSettings.setActive(true);
            deliverySettingsRepository.save(defaultSettings);

            redirectAttributes.addFlashAttribute("success", "✅ Delivery settings reset to default values!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Failed to reset settings: " + e.getMessage());
        }
        return "redirect:/staff/delivery-settings";
    }

    // API endpoint to get current settings (for AJAX calls)
    @GetMapping("/api/current")
    @ResponseBody
    public DeliverySettings getCurrentSettings() {
        return deliverySettingsRepository.findActive();
    }
}