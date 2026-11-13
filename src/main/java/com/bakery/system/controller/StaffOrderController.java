package com.bakery.system.controller;

import com.bakery.system.entity.Order;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.service.EmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff/orders")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
public class StaffOrderController {

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public StaffOrderController(OrderRepository orderRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findAll().stream()
                    .filter(o -> o.getStatus().equalsIgnoreCase(status))
                    .toList();
        } else {
            orders = orderRepository.findAllByOrderByOrderDateDesc();
        }
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        return "staff-orders";
    }

    @GetMapping("/{id}")
    @Transactional
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // Force initialize the items collection to avoid LazyInitializationException
        order.getItems().size();
        model.addAttribute("order", order);
        return "staff-order-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        String oldStatus = order.getStatus();
        order.setStatus(status);
        orderRepository.save(order);

        if ("CONFIRMED".equals(status) && !"CONFIRMED".equals(oldStatus)) {
            emailService.sendOrderConfirmationEmail(order);
            redirectAttributes.addFlashAttribute("emailSent", true);
        }

        return "redirect:/staff/orders/" + id + "?updated";
    }
}