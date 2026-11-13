package com.bakery.system.controller;

import com.bakery.system.entity.DeliveryAssignment;
import com.bakery.system.entity.DeliveryPersonnel;
import com.bakery.system.repository.DeliveryAssignmentRepository;
import com.bakery.system.repository.DeliveryPersonnelRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/driver")
public class DriverController {

    private final DeliveryPersonnelRepository personnelRepository;
    private final DeliveryAssignmentRepository assignmentRepository;

    // Constructor injection - make sure both repositories are injected
    public DriverController(DeliveryPersonnelRepository personnelRepository,
                            DeliveryAssignmentRepository assignmentRepository) {
        this.personnelRepository = personnelRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/login")
    public String driverLoginPage() {
        return "driver-login";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('DRIVER')")
    public String driverDashboard(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // Get driver by username (which is their email)
            String email = principal.getName();
            DeliveryPersonnel driver = personnelRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found with email: " + email));

            // Get active assignments (not delivered)
            List<DeliveryAssignment> activeAssignments = assignmentRepository
                    .findByDeliveryPersonnelAndStatusIn(driver, List.of("ASSIGNED", "PICKED_UP", "IN_TRANSIT"));

            // Get completed deliveries count
            Long completedCount = assignmentRepository.countCompletedDeliveriesByPersonnel(driver.getId());

            model.addAttribute("driver", driver);
            model.addAttribute("assignments", activeAssignments);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("totalDeliveries", driver.getCompletedDeliveries());

            return "driver-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "redirect:/driver/login";
        }
    }

    @PostMapping("/update-status/{assignmentId}")
    @PreAuthorize("hasRole('DRIVER')")
    public String updateStatus(@PathVariable Long assignmentId,
                               @RequestParam String status,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get driver by email
            String email = principal.getName();
            DeliveryPersonnel driver = personnelRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found with email: " + email));

            // Get assignment
            DeliveryAssignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

            // Verify this driver owns this assignment
            if (!assignment.getDeliveryPersonnel().getId().equals(driver.getId())) {
                throw new IllegalArgumentException("You are not authorized to update this delivery");
            }

            assignment.setStatus(status);
            assignmentRepository.save(assignment);

            // Update driver status based on delivery status
            if ("DELIVERED".equals(status)) {
                driver.setCompletedDeliveries(driver.getCompletedDeliveries() + 1);
                driver.setStatus("AVAILABLE");
                personnelRepository.save(driver);
                redirectAttributes.addFlashAttribute("success", "✅ Delivery completed! Thank you for your service!");
            } else if ("PICKED_UP".equals(status)) {
                driver.setStatus("BUSY");
                personnelRepository.save(driver);
                redirectAttributes.addFlashAttribute("success", "📦 Order picked up! Update status when in transit.");
            } else if ("IN_TRANSIT".equals(status)) {
                driver.setStatus("BUSY");
                personnelRepository.save(driver);
                redirectAttributes.addFlashAttribute("success", "🚚 In transit! Update status when delivered.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error updating status: " + e.getMessage());
        }
        return "redirect:/driver/dashboard";
    }
}