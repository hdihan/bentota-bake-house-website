package com.bakery.system.service;

import com.bakery.system.entity.Order;
import com.bakery.system.entity.Payment;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class DemoPaymentService {
    private static final Logger logger = LoggerFactory.getLogger(DemoPaymentService.class);
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public DemoPaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Payment processPayment(Long orderId, Map<String, String> paymentDetails) {
        try {
            logger.info("Processing payment for order: {}", orderId);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            String cardNumber = paymentDetails.get("cardNumber").replaceAll("\\s", "");
            String expiryDate = paymentDetails.get("expiryDate");
            String cvv = paymentDetails.get("cvv");
            String cardName = paymentDetails.get("cardName");
            String selectedCardType = paymentDetails.get("cardType");

            logger.info("Processing payment for card type: {}", selectedCardType);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setPaymentDate(LocalDateTime.now());

            // Validate card type is selected
            if (selectedCardType == null || selectedCardType.isEmpty()) {
                payment.setStatus("FAILED");
                payment.setPaymentId("FAILED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                paymentRepository.save(payment);
                return payment;
            }

            // Validate CVV (3 digits)
            if (cvv == null || !cvv.matches("\\d{3}")) {
                payment.setStatus("FAILED");
                payment.setPaymentId("FAILED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                paymentRepository.save(payment);
                return payment;
            }

            // Validate expiry date
            if (!isValidExpiryDate(expiryDate)) {
                payment.setStatus("FAILED");
                payment.setPaymentId("FAILED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                paymentRepository.save(payment);
                return payment;
            }

            // Validate card number (16 digits)
            if (cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) {
                payment.setStatus("FAILED");
                payment.setPaymentId("FAILED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                paymentRepository.save(payment);
                return payment;
            }

            // For demo purposes, accept any valid format card
            // In production, you would integrate with a real payment gateway
            payment.setStatus("SUCCESS");
            String transactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            payment.setPaymentId(transactionId);
            payment.setCardType(selectedCardType);
            order.setPaymentStatus("PAID");
            order.setStatus("CONFIRMED");
            orderRepository.save(order);

            logger.info("Payment SUCCESSFUL for order {}: Transaction ID: {}", orderId, transactionId);

            paymentRepository.save(payment);
            return payment;

        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            Payment payment = new Payment();
            payment.setStatus("FAILED");
            payment.setPaymentId("ERROR_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            return payment;
        }
    }

    private boolean isValidExpiryDate(String expiryDate) {
        try {
            if (expiryDate == null || expiryDate.trim().isEmpty()) {
                return false;
            }

            if (!expiryDate.matches("\\d{2}/\\d{2}")) {
                return false;
            }

            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000;

            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();

            if (month < 1 || month > 12) {
                return false;
            }

            if (year < currentYear) {
                return false;
            }
            if (year == currentYear && month < currentMonth) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getPaymentSummary(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return Map.of(
                "orderId", order.getId(),
                "amount", order.getTotalAmount(),
                "items", order.getItems().size()
        );
    }
}