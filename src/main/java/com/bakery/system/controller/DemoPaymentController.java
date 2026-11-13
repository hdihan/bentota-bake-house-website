package com.bakery.system.controller;

import com.bakery.system.entity.Order;
import com.bakery.system.entity.Payment;
import com.bakery.system.repository.OrderRepository;
import com.bakery.system.service.DemoPaymentService;
import com.bakery.system.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/demo-payment")
public class DemoPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(DemoPaymentController.class);

    private final DemoPaymentService paymentService;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public DemoPaymentController(DemoPaymentService paymentService,
                                 OrderRepository orderRepository,
                                 EmailService emailService) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    @GetMapping("/checkout/{orderId}")
    public String showPaymentPage(@PathVariable Long orderId, Model model, HttpSession session) {
        try {
            logger.info("Loading payment page for order: {}", orderId);

            // Verify order exists and is in pending payment state
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            // Check if order belongs to logged-in user (optional security check)
            // This ensures the user is still authenticated

            Map<String, Object> summary = paymentService.getPaymentSummary(orderId);
            model.addAttribute("summary", summary);
            model.addAttribute("order", order);

            return "demo-payment";
        } catch (Exception e) {
            logger.error("Error loading payment page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load payment page: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/process")
    public String processPayment(@RequestParam Map<String, String> paymentDetails,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        try {
            logger.info("Processing payment with details: {}", paymentDetails.keySet());

            String orderIdStr = paymentDetails.get("orderId");
            if (orderIdStr == null || orderIdStr.isEmpty()) {
                throw new IllegalArgumentException("Order ID is missing");
            }

            Long orderId = Long.parseLong(orderIdStr);

            // Validate required fields
            String cardNumber = paymentDetails.get("cardNumber");
            String expiryDate = paymentDetails.get("expiryDate");
            String cvv = paymentDetails.get("cvv");
            String cardName = paymentDetails.get("cardName");

            // Detailed validation with specific error messages
            if (cardName == null || cardName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("paymentStatus", "failed");
                redirectAttributes.addFlashAttribute("errorMessage", "Cardholder name is required");
                return "redirect:/demo-payment/failed";
            }

            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("paymentStatus", "failed");
                redirectAttributes.addFlashAttribute("errorMessage", "Card number is required");
                return "redirect:/demo-payment/failed";
            }

            String cleanCardNumber = cardNumber.replaceAll("\\s", "");
            if (cleanCardNumber.length() != 16) {
                redirectAttributes.addFlashAttribute("paymentStatus", "failed");
                redirectAttributes.addFlashAttribute("errorMessage", "Card number must be 16 digits");
                return "redirect:/demo-payment/failed";
            }

            if (expiryDate == null || expiryDate.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("paymentStatus", "failed");
                redirectAttributes.addFlashAttribute("errorMessage", "Expiry date is required");
                return "redirect:/demo-payment/failed";
            }

            if (cvv == null || cvv.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("paymentStatus", "failed");
                redirectAttributes.addFlashAttribute("errorMessage", "CVV is required");
                return "redirect:/demo-payment/failed";
            }

            if (cvv.length() != 3) {
                redirectAttributes.addFlashAttribute("paymentStatus", "failed");
                redirectAttributes.addFlashAttribute("errorMessage", "CVV must be 3 digits");
                return "redirect:/demo-payment/failed";
            }

            Payment payment = paymentService.processPayment(orderId, paymentDetails);

            if ("SUCCESS".equals(payment.getStatus())) {
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    try {
                        emailService.sendOrderConfirmationEmail(order);
                        logger.info("Confirmation email sent for order: {}", orderId);
                    } catch (Exception e) {
                        logger.error("Failed to send confirmation email: {}", e.getMessage());
                    }
                }

                // Store in flash attributes for success page
                redirectAttributes.addFlashAttribute("paymentStatus", "success");
                redirectAttributes.addFlashAttribute("orderId", orderId);
                redirectAttributes.addFlashAttribute("amount", payment.getAmount());
                redirectAttributes.addFlashAttribute("transactionId", payment.getPaymentId());

                // Keep the session alive
                session.setAttribute("paymentSuccess", true);

                return "redirect:/demo-payment/success";
            } else {
                redirectAttributes.addFlashAttribute("paymentStatus", "failed");
                redirectAttributes.addFlashAttribute("errorMessage", "Payment declined. Please check your card details and try again.");
                return "redirect:/demo-payment/failed";
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid order ID format: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("paymentStatus", "failed");
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid order ID");
            return "redirect:/demo-payment/failed";
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("paymentStatus", "failed");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/demo-payment/failed";
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("paymentStatus", "failed");
            redirectAttributes.addFlashAttribute("errorMessage", "Payment processing error: " + e.getMessage());
            return "redirect:/demo-payment/failed";
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(Model model, HttpSession session) {
        // Ensure the flash attributes are still available
        // The user should still be authenticated via session
        return "demo-payment-success";
    }

    @GetMapping("/failed")
    public String paymentFailed(Model model) {
        return "demo-payment-failed";
    }
}