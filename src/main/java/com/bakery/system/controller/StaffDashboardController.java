package com.bakery.system.controller;

import com.bakery.system.entity.CateringRequest;
import com.bakery.system.entity.Order;
import com.bakery.system.entity.Product;
import com.bakery.system.entity.User;
import com.bakery.system.repository.CateringRequestRepository;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
public class StaffDashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CateringRequestRepository cateringRequestRepository;
    private final UserRepository userRepository;

    public StaffDashboardController(OrderRepository orderRepository,
                                    ProductRepository productRepository,
                                    CateringRequestRepository cateringRequestRepository,
                                    UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cateringRequestRepository = cateringRequestRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String staffDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "Unknown";
        User currentUser = userRepository.findByUsername(username);

        // Safe fallback for missing user
        String userRole = (currentUser != null && currentUser.getRole() != null)
                ? currentUser.getRole() : "ROLE_STAFF";
        String displayName = (currentUser != null) ? currentUser.getUsername() : username;
        boolean isAdmin = "ROLE_ADMIN".equals(userRole) || "ROLE_SUPER_ADMIN".equals(userRole);
        boolean isStaff = "ROLE_STAFF".equals(userRole);

        long pendingOrders = orderRepository.findAll().stream()
                .filter(order -> "PENDING".equals(order.getStatus()))
                .count();

        long lowStockCount = productRepository.findAll().stream()
                .filter(product -> product.getStockQuantity() != null && product.getStockQuantity() < 5)
                .count();

        long activeCatering = cateringRequestRepository.findAll().stream()
                .filter(request -> !"DELIVERED".equals(request.getStatus())
                        && !"COMPLETED".equals(request.getStatus())
                        && !"CANCELLED".equals(request.getStatus()))
                .count();

        long totalProducts = productRepository.count();

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("activeCatering", activeCatering);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);
        model.addAttribute("userName", displayName);
        model.addAttribute("userRole", userRole.replace("ROLE_", ""));

        return "staff-dashboard";
    }
}