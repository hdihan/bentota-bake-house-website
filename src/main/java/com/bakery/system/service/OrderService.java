package com.bakery.system.service;

import com.bakery.system.entity.*;
import com.bakery.system.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final StockMovementRepository stockMovementRepository;  // ← ADDED

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CartService cartService,
                        StockMovementRepository stockMovementRepository) {  // ← ADDED PARAMETER
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.stockMovementRepository = stockMovementRepository;  // ← ADDED
    }

    @Transactional
    public Order createOrder(User user, HttpSession session,
                             String orderType, String deliveryMethod,
                             String deliveryAddress, LocalDateTime pickupTime,
                             String paymentMethod, String notes) {

        // 1. Get cart items
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 2. Check stock and calculate total
        double total = 0.0;
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            if (product.getStockQuantity() < entry.getValue()) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }
            total += product.getPrice() * entry.getValue();
        }

        // 3. Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderType(orderType);
        order.setStatus("PENDING");
        order.setTotalAmount(total);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");
        order.setDeliveryMethod(deliveryMethod);
        order.setDeliveryAddress(deliveryAddress);
        order.setPickupTime(pickupTime);
        order.setNotes(notes);

        // 4. Create order items, reduce stock, and track movements
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).get();
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(entry.getValue());
            item.setPriceAtOrder(product.getPrice());
            order.getItems().add(item);

            // Reduce stock
            int oldStock = product.getStockQuantity();
            product.setStockQuantity(product.getStockQuantity() - entry.getValue());
            productRepository.save(product);

            // Track stock movement (OUT)
            StockMovement movement = new StockMovement();
            movement.setProduct(product);
            movement.setMovementType("OUT");
            movement.setQuantity(entry.getValue());
            movement.setReason("Order placed - Order #" + (order.getId() != null ? order.getId() : "Pending"));
            movement.setPerformedBy(user.getUsername());
            stockMovementRepository.save(movement);
        }

        // 5. Save order
        Order savedOrder = orderRepository.save(order);

        // 6. Update the movement records with the actual order ID (for pending orders where ID was null)
        for (StockMovement movement : stockMovementRepository.findByMovementDateBetween(
                LocalDateTime.now().minusMinutes(1), LocalDateTime.now())) {
            if (movement.getReason() != null && movement.getReason().contains("Order #Pending")) {
                movement.setReason("Order placed - Order #" + savedOrder.getId());
                stockMovementRepository.save(movement);
            }
        }

        // 7. Clear cart
        cartService.clearCart(session);

        return savedOrder;
    }
}