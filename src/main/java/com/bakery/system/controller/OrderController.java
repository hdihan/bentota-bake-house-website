package com.bakery.system.controller;

import com.bakery.system.entity.Order;
import com.bakery.system.entity.Product;
import com.bakery.system.entity.User;
import com.bakery.system.entity.StockMovement;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.UserRepository;
import com.bakery.system.repository.StockMovementRepository;
import com.bakery.system.service.CartService;
import com.bakery.system.service.EmailService;
import com.bakery.system.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final StockMovementRepository stockMovementRepository;

    public OrderController(OrderService orderService,
                           OrderRepository orderRepository,
                           UserRepository userRepository,
                           CartService cartService,
                           ProductRepository productRepository,
                           EmailService emailService,
                           StockMovementRepository stockMovementRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.emailService = emailService;
        this.stockMovementRepository = stockMovementRepository;
    }

    @GetMapping("/checkout")
    public String showCheckoutForm(Model model, HttpSession session) {
        if (cartService.getCartItems(session).isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("total", cartService.getTotalAmount(session));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String orderType,
                             @RequestParam String deliveryMethod,
                             @RequestParam(required = false) String deliveryAddress,
                             @RequestParam(required = false) String pickupDateTime,
                             @RequestParam String paymentMethod,
                             @RequestParam(required = false) String notes,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session) {

        User user = userRepository.findByUsername(userDetails.getUsername());

        LocalDateTime pickupTime = null;
        if (pickupDateTime != null && !pickupDateTime.isEmpty()) {
            pickupTime = LocalDateTime.parse(pickupDateTime);
        }

        if ("ONLINE".equals(orderType) && (deliveryAddress == null || deliveryAddress.isEmpty())) {
            deliveryAddress = user.getHomeAddress();
        }

        Order order = orderService.createOrder(user, session, orderType, deliveryMethod,
                deliveryAddress, pickupTime, paymentMethod, notes);

        if ("ONLINE".equals(paymentMethod)) {
            // For online payment, redirect to demo payment page
            return "redirect:/demo-payment/checkout/" + order.getId();
        } else {
            // For COD, send received email and redirect to confirmation
            emailService.sendOrderReceivedEmail(order);
            return "redirect:/order/confirmation/" + order.getId();
        }
    }

    @GetMapping("/order/confirmation/{id}")
    public String orderConfirmation(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        model.addAttribute("order", order);
        return "order-confirmation";
    }

    @GetMapping("/my-orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    @GetMapping("/order/{id}")
    public String viewOrder(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        User currentUser = userRepository.findByUsername(userDetails.getUsername());

        if (!order.getUser().getId().equals(currentUser.getId()) &&
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"))) {
            return "redirect:/my-orders?error=unauthorized";
        }

        model.addAttribute("order", order);
        return "order-detail";
    }

    @PostMapping("/order/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        User currentUser = userRepository.findByUsername(userDetails.getUsername());

        if (!order.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to cancel this order.");
            return "redirect:/my-orders";
        }

        if (!"PENDING".equals(order.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Only pending orders can be cancelled.");
            return "redirect:/my-orders";
        }

        // Restore stock and track movements
        for (com.bakery.system.entity.OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);

            // Track stock movement (IN - due to cancellation)
            StockMovement movement = new StockMovement();
            movement.setProduct(product);
            movement.setMovementType("IN");
            movement.setQuantity(item.getQuantity());
            movement.setReason("Order cancelled - Order #" + order.getId());
            movement.setPerformedBy(currentUser.getUsername());
            stockMovementRepository.save(movement);
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("success", "Order cancelled successfully.");
        return "redirect:/my-orders";
    }
}