package com.bakery.system.controller;

import com.bakery.system.entity.*;
import com.bakery.system.repository.*;
import com.bakery.system.service.DeliveryAssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/staff/delivery")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
public class DeliveryController {

    private final DeliveryAssignmentService deliveryService;
    private final DeliveryPersonnelRepository personnelRepository;
    private final OrderRepository orderRepository;
    private final CateringRequestRepository cateringRequestRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public DeliveryController(DeliveryAssignmentService deliveryService,
                              DeliveryPersonnelRepository personnelRepository,
                              OrderRepository orderRepository,
                              CateringRequestRepository cateringRequestRepository,
                              DeliveryAssignmentRepository assignmentRepository,
                              UserRepository userRepository) {
        this.deliveryService = deliveryService;
        this.personnelRepository = personnelRepository;
        this.orderRepository = orderRepository;
        this.cateringRequestRepository = cateringRequestRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    @Transactional
    public String deliveryDashboard(Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get active assignments and initialize lazy collections
            List<DeliveryAssignment> activeAssignments = assignmentRepository.findActiveAssignments();
            for (DeliveryAssignment assignment : activeAssignments) {
                if (assignment.getDeliveryPersonnel() != null) {
                    // Force initialize to avoid LazyInitializationException
                    assignment.getDeliveryPersonnel().getName();
                }
            }
            model.addAttribute("activeAssignments", activeAssignments);

            // Get available personnel
            List<DeliveryPersonnel> availablePersonnel = personnelRepository.findByStatus("AVAILABLE");
            model.addAttribute("availablePersonnel", availablePersonnel);

            // Stats
            long totalPersonnel = personnelRepository.count();
            long busyPersonnel = personnelRepository.findByStatus("BUSY").size();
            model.addAttribute("totalPersonnel", totalPersonnel);
            model.addAttribute("busyPersonnel", busyPersonnel);

            // Pending orders (CONFIRMED status, not pickup)
            List<Order> pendingOrders = orderRepository.findAll().stream()
                    .filter(o -> "CONFIRMED".equals(o.getStatus()) && !"PICKUP".equals(o.getDeliveryMethod()))
                    .toList();
            model.addAttribute("pendingOrders", pendingOrders);

            // Pending catering requests (CONFIRMED status)
            List<CateringRequest> pendingCatering = cateringRequestRepository.findByStatus("CONFIRMED");
            model.addAttribute("pendingCatering", pendingCatering);

            return "delivery-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    // ==================== PERSONNEL MANAGEMENT ====================

    @GetMapping("/personnel")
    public String managePersonnel(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<DeliveryPersonnel> allPersonnel = personnelRepository.findAll();
            model.addAttribute("personnel", allPersonnel);
            model.addAttribute("newPersonnel", new DeliveryPersonnel());
            return "delivery-personnel";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading personnel: " + e.getMessage());
            return "redirect:/staff/delivery/dashboard";
        }
    }

    @PostMapping("/personnel/add")
    public String addPersonnel(@ModelAttribute DeliveryPersonnel personnel,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate phone number (10 digits)
            if (personnel.getPhone() == null || !personnel.getPhone().matches("^[0-9]{10}$")) {
                redirectAttributes.addFlashAttribute("error", "❌ Phone number must be exactly 10 digits!");
                return "redirect:/staff/delivery/personnel";
            }

            // Validate email format
            if (personnel.getEmail() == null || !personnel.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                redirectAttributes.addFlashAttribute("error", "❌ Please enter a valid email address!");
                return "redirect:/staff/delivery/personnel";
            }

            // Check for duplicate phone
            if (personnelRepository.existsByPhone(personnel.getPhone())) {
                redirectAttributes.addFlashAttribute("error", "❌ Phone number already registered!");
                return "redirect:/staff/delivery/personnel";
            }

            // Check for duplicate email
            if (personnelRepository.existsByEmail(personnel.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "❌ Email already registered!");
                return "redirect:/staff/delivery/personnel";
            }

            personnel.setStatus("AVAILABLE");
            personnel.setCompletedDeliveries(0);
            personnel.setRating(5.0);

            personnelRepository.save(personnel);
            redirectAttributes.addFlashAttribute("success", "✅ Driver '" + personnel.getName() + "' added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error adding driver: " + e.getMessage());
        }
        return "redirect:/staff/delivery/personnel";
    }

    @PostMapping("/personnel/update")
    public String updatePersonnel(@ModelAttribute DeliveryPersonnel personnel,
                                  RedirectAttributes redirectAttributes) {
        try {
            DeliveryPersonnel existing = personnelRepository.findById(personnel.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Personnel not found"));

            // Validate phone number
            if (personnel.getPhone() == null || !personnel.getPhone().matches("^[0-9]{10}$")) {
                redirectAttributes.addFlashAttribute("error", "❌ Phone number must be exactly 10 digits!");
                return "redirect:/staff/delivery/personnel";
            }

            existing.setName(personnel.getName());
            existing.setPhone(personnel.getPhone());
            existing.setEmail(personnel.getEmail());
            existing.setVehicleType(personnel.getVehicleType());
            existing.setVehicleNumber(personnel.getVehicleNumber());
            existing.setStatus(personnel.getStatus());

            personnelRepository.save(existing);
            redirectAttributes.addFlashAttribute("success", "✅ Driver '" + existing.getName() + "' updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error updating driver: " + e.getMessage());
        }
        return "redirect:/staff/delivery/personnel";
    }

    @GetMapping("/personnel/delete/{id}")
    public String deletePersonnel(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            DeliveryPersonnel personnel = personnelRepository.findById(id).orElse(null);
            if (personnel == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Driver not found!");
            } else if (!personnel.getAssignments().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "❌ Cannot delete driver '" + personnel.getName() + "' because they have active or past deliveries!");
            } else {
                String name = personnel.getName();
                personnelRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "✅ Driver '" + name + "' deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error deleting driver: " + e.getMessage());
        }
        return "redirect:/staff/delivery/personnel";
    }

    // ==================== ASSIGN ORDER DELIVERY ====================

    @GetMapping("/assign-order/{orderId}")
    public String showAssignOrderForm(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            List<DeliveryPersonnel> availablePersonnel = personnelRepository.findByStatus("AVAILABLE");

            model.addAttribute("order", order);
            model.addAttribute("availablePersonnel", availablePersonnel);
            return "assign-order-delivery";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/staff/delivery/dashboard";
        }
    }

    @PostMapping("/assign-order")
    public String assignOrder(@RequestParam Long orderId,
                              @RequestParam Long personnelId,
                              @RequestParam(required = false) String notes,
                              RedirectAttributes redirectAttributes) {
        try {
            DeliveryAssignment assignment = deliveryService.assignOrderToPersonnel(orderId, personnelId, notes);
            DeliveryPersonnel driver = personnelRepository.findById(personnelId).orElse(null);
            redirectAttributes.addFlashAttribute("success", "✅ Order #" + orderId + " assigned to driver '" +
                    (driver != null ? driver.getName() : "unknown") + "' successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Assignment failed: " + e.getMessage());
        }
        return "redirect:/staff/delivery/dashboard";
    }

    // ==================== ASSIGN CATERING DELIVERY ====================

    @GetMapping("/assign-catering/{cateringId}")
    public String showAssignCateringForm(@PathVariable Long cateringId, Model model, RedirectAttributes redirectAttributes) {
        try {
            CateringRequest catering = cateringRequestRepository.findById(cateringId)
                    .orElseThrow(() -> new IllegalArgumentException("Catering request not found"));

            // For large events (50+ guests), only show personnel with TRUCK
            boolean needsTruck = catering.getNumberOfGuests() > 50;
            List<DeliveryPersonnel> availablePersonnel;

            if (needsTruck) {
                availablePersonnel = personnelRepository.findByStatus("AVAILABLE").stream()
                        .filter(p -> "Truck".equals(p.getVehicleType()))
                        .toList();
            } else {
                availablePersonnel = personnelRepository.findByStatus("AVAILABLE");
            }

            model.addAttribute("catering", catering);
            model.addAttribute("availablePersonnel", availablePersonnel);
            model.addAttribute("needsTruck", needsTruck);
            return "assign-catering-delivery";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/staff/delivery/dashboard";
        }
    }

    @PostMapping("/assign-catering")
    public String assignCatering(@RequestParam Long cateringId,
                                 @RequestParam Long personnelId,
                                 @RequestParam(required = false) String notes,
                                 RedirectAttributes redirectAttributes) {
        try {
            DeliveryAssignment assignment = deliveryService.assignCateringToPersonnel(cateringId, personnelId, notes);
            DeliveryPersonnel driver = personnelRepository.findById(personnelId).orElse(null);
            CateringRequest catering = cateringRequestRepository.findById(cateringId).orElse(null);

            redirectAttributes.addFlashAttribute("success", "✅ Catering delivery for '" +
                    (catering != null ? catering.getCustomer().getFullName() : "customer") +
                    "' assigned to driver '" + (driver != null ? driver.getName() : "unknown") +
                    "' successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Assignment failed: " + e.getMessage());
        }
        return "redirect:/staff/delivery/dashboard";
    }

    // ==================== UPDATE STATUS (MANAGER) ====================

    @PostMapping("/update-status/{assignmentId}")
    public String updateStatus(@PathVariable Long assignmentId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            DeliveryAssignment assignment = deliveryService.updateAssignmentStatus(assignmentId, status);
            String statusDisplay = status.replace("_", " ");
            redirectAttributes.addFlashAttribute("success", "✅ Delivery status updated to: " + statusDisplay + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error updating status: " + e.getMessage());
        }
        return "redirect:/staff/delivery/dashboard";
    }

    // ==================== DRIVER SELF-SERVICE ENDPOINTS ====================

    @GetMapping("/driver/dashboard")
    @PreAuthorize("hasRole('DRIVER')")
    @Transactional
    public String driverDashboard(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // Get driver by email (username is email for drivers)
            String email = principal.getName();
            DeliveryPersonnel driver = personnelRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found with email: " + email));

            // Get active assignments (not delivered or cancelled)
            List<DeliveryAssignment> activeAssignments = assignmentRepository
                    .findByDeliveryPersonnelAndStatusIn(driver, List.of("ASSIGNED", "PICKED_UP", "IN_TRANSIT"));

            // Force initialize to avoid lazy loading issues
            for (DeliveryAssignment assignment : activeAssignments) {
                assignment.getCustomerName();
                assignment.getDeliveryAddress();
            }

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

    @PostMapping("/driver/update-status/{assignmentId}")
    @PreAuthorize("hasRole('DRIVER')")
    public String driverUpdateStatus(@PathVariable Long assignmentId,
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
                redirectAttributes.addFlashAttribute("success", "📦 Order picked up! Please update status when in transit.");
            } else if ("IN_TRANSIT".equals(status)) {
                driver.setStatus("BUSY");
                personnelRepository.save(driver);
                redirectAttributes.addFlashAttribute("success", "🚚 In transit! Please update status when delivered.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error updating status: " + e.getMessage());
        }
        return "redirect:/driver/dashboard";
    }

    // ==================== API ENDPOINTS ====================

    @GetMapping("/assignments")
    @ResponseBody
    public List<DeliveryAssignment> getActiveAssignments() {
        return assignmentRepository.findActiveAssignments();
    }

    @GetMapping("/personnel/available")
    @ResponseBody
    public List<DeliveryPersonnel> getAvailablePersonnel() {
        return personnelRepository.findByStatus("AVAILABLE");
    }
}