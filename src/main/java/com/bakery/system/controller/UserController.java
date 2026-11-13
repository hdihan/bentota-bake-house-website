package com.bakery.system.controller;

import com.bakery.system.entity.User;
import com.bakery.system.repository.UserRepository;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.repository.CateringRequestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CateringRequestRepository cateringRequestRepository;

    public UserController(UserRepository userRepository,
                          OrderRepository orderRepository,
                          CateringRequestRepository cateringRequestRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.cateringRequestRepository = cateringRequestRepository;
    }

    /**
     * Get current logged-in user's role and username
     */
    @GetMapping("/user/role")
    public Map<String, String> getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("username", "guest");
            response.put("role", "ROLE_CUSTOMER");
            return response;
        }

        String username = authentication.getName();
        String role = "ROLE_CUSTOMER";

        try {
            User user = userRepository.findByUsername(username);
            if (user != null && user.getRole() != null) {
                role = user.getRole();
            }
        } catch (Exception e) {
            if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                role = authentication.getAuthorities().iterator().next().getAuthority();
            }
        }

        response.put("username", username);
        response.put("role", role);
        return response;
    }

    /**
     * Get pending orders count
     */
    @GetMapping("/orders/pending-count")
    public Map<String, Long> getPendingOrdersCount() {
        Map<String, Long> response = new HashMap<>();
        try {
            long count = orderRepository.countByStatus("PENDING");
            response.put("count", count);
        } catch (Exception e) {
            response.put("count", 0L);
        }
        return response;
    }

    /**
     * Get active catering requests count
     */
    @GetMapping("/catering/active-count")
    public Map<String, Long> getActiveCateringCount() {
        Map<String, Long> response = new HashMap<>();
        try {
            long count = cateringRequestRepository.countActiveRequests();
            response.put("count", count);
        } catch (Exception e) {
            response.put("count", 0L);
        }
        return response;
    }
}