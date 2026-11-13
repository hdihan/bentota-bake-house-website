package com.bakery.system.controller;

import com.bakery.system.entity.CateringRequest;
import com.bakery.system.entity.Order;
import com.bakery.system.entity.Product;
import com.bakery.system.repository.CateringRequestRepository;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/staff/api")
public class StaffApiController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CateringRequestRepository cateringRequestRepository;

    public StaffApiController(OrderRepository orderRepository,
                              ProductRepository productRepository,
                              CateringRequestRepository cateringRequestRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cateringRequestRepository = cateringRequestRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            long totalProducts = productRepository.count();
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

            stats.put("totalProducts", totalProducts);
            stats.put("pendingOrders", pendingOrders);
            stats.put("lowStockCount", lowStockCount);
            stats.put("activeCatering", activeCatering);
        } catch (Exception e) {
            stats.put("totalProducts", 0);
            stats.put("pendingOrders", 0);
            stats.put("lowStockCount", 0);
            stats.put("activeCatering", 0);
        }

        return stats;
    }

    @GetMapping("/recent-activities")
    public Map<String, Object> getRecentActivities() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> activities = new ArrayList<>();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            List<Order> recentOrders = orderRepository.findAllByOrderByOrderDateDesc()
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            for (Order order : recentOrders) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("message", "New order #" + order.getId() + " from " + order.getUser().getUsername());
                activity.put("time", order.getOrderDate().format(formatter));
                activity.put("icon", "shopping-cart");
                activity.put("color", "primary");
                activities.add(activity);
            }

            List<CateringRequest> recentCatering = cateringRequestRepository.findAll()
                    .stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .collect(Collectors.toList());

            for (CateringRequest request : recentCatering) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("message", "Catering request #" + request.getId() + " from " + request.getCustomer().getUsername() + " - " + request.getStatus());
                activity.put("time", request.getCreatedAt().format(formatter));
                activity.put("icon", "utensils");
                activity.put("color", "warning");
                activities.add(activity);
            }

            activities.sort((a, b) -> b.get("time").toString().compareTo(a.get("time").toString()));
            result.put("activities", activities.stream().limit(10).collect(Collectors.toList()));
        } catch (Exception e) {
            result.put("activities", new ArrayList<>());
        }

        return result;
    }
}