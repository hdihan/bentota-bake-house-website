package com.bakery.system.service;

import com.bakery.system.entity.CateringRequest;
import com.bakery.system.entity.Order;
import com.bakery.system.entity.Product;
import com.bakery.system.entity.InventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ===========================
    // Order emails
    // ===========================

    /**
     * Send an email for an order that has been received (pending).
     */
    public void sendOrderReceivedEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUser().getEmail());
            message.setSubject("Order #" + order.getId() + " Received – Thank You!");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "We have received your order #%d.\n" +
                            "Order Total: Rs. %.2f\n" +
                            "Payment Method: %s\n" +
                            "Delivery Method: %s\n" +
                            "Delivery Address: %s\n" +
                            "Pickup Time: %s\n\n" +
                            "We will notify you once your order is confirmed.\n\n" +
                            "Thank you for shopping with Benthota Bake House!",
                    order.getUser().getFullName(),
                    order.getId(),
                    order.getTotalAmount(),
                    order.getPaymentMethod(),
                    order.getDeliveryMethod(),
                    order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "N/A",
                    order.getPickupTime() != null ? order.getPickupTime() : "N/A"
            ));
            mailSender.send(message);
            System.out.println("Order received email sent to " + order.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send order received email for order #" + order.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Send an email for an order that has been confirmed (e.g., after payment or staff approval).
     */
    public void sendOrderConfirmationEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUser().getEmail());
            message.setSubject("Your Order #" + order.getId() + " is Confirmed!");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your order #%d has been confirmed and is now being processed.\n" +
                            "Order Total: Rs. %.2f\n" +
                            "Payment Method: %s\n" +
                            "Delivery Method: %s\n" +
                            "Delivery Address: %s\n" +
                            "Pickup Time: %s\n\n" +
                            "Thank you for shopping with Benthota Bake House!",
                    order.getUser().getFullName(),
                    order.getId(),
                    order.getTotalAmount(),
                    order.getPaymentMethod(),
                    order.getDeliveryMethod(),
                    order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "N/A",
                    order.getPickupTime() != null ? order.getPickupTime() : "N/A"
            ));
            mailSender.send(message);
            System.out.println("Order confirmation email sent to " + order.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email for order #" + order.getId() + ": " + e.getMessage());
        }
    }

    // ===========================
    // Catering emails
    // ===========================

    /**
     * Send a detailed quotation email for a catering request.
     */
    public void sendCateringQuotation(CateringRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getCustomer().getEmail());
            message.setSubject("Your Catering Request #" + request.getId() + " – Quotation Received");

            StringBuilder text = new StringBuilder();
            text.append("Dear ").append(request.getCustomer().getFullName()).append(",\n\n");
            text.append("We have prepared a quotation for your catering request #").append(request.getId()).append(".\n");
            text.append("Event: ").append(request.getEventDate()).append(" at ").append(request.getEventTime()).append("\n");
            text.append("Location: ").append(request.getLocation()).append("\n");
            text.append("Number of guests: ").append(request.getNumberOfGuests()).append("\n\n");

            text.append("Items ordered:\n");
            for (var item : request.getItems()) {
                text.append("- ").append(item.getItemName());
                if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                    text.append(" (").append(item.getDescription()).append(")");
                }
                text.append(" x ").append(item.getQuantity());
                if (item.getQuotedUnitPrice() != null) {
                    text.append(" @ Rs. ").append(item.getQuotedUnitPrice());
                    text.append(" = Rs. ").append(item.getQuotedUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                text.append("\n");
            }
            text.append("\n");

            if (request.getServiceCharge() != null && request.getServiceCharge().compareTo(BigDecimal.ZERO) > 0) {
                text.append("Service Charge: Rs. ").append(request.getServiceCharge()).append("\n");
            }
            if (request.getDistance() != null) {
                text.append("Distance from bakery: ").append(request.getDistance()).append(" km\n");
            }
            if (request.getDeliveryFee() != null && request.getDeliveryFee() > 0) {
                text.append("Delivery Fee: Rs. ").append(request.getDeliveryFee()).append("\n");
            }

            text.append("Total Quoted Price: Rs. ").append(request.getTotalQuotedPrice()).append("\n\n");

            if (request.getManagerNotes() != null && !request.getManagerNotes().isEmpty()) {
                text.append("Manager's Notes:\n").append(request.getManagerNotes()).append("\n\n");
            }

            text.append("Please log in to your account to confirm or ask questions.\n");
            text.append("Thank you for choosing Benthota Bake House!");

            message.setText(text.toString());
            mailSender.send(message);
            System.out.println("Catering quotation email sent to " + request.getCustomer().getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send catering quotation email for request #" + request.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Send a status update email for a catering request.
     */
    public void sendCateringStatusUpdate(CateringRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getCustomer().getEmail());
            message.setSubject("Update on Your Catering Request #" + request.getId());

            StringBuilder text = new StringBuilder();
            text.append("Dear ").append(request.getCustomer().getFullName()).append(",\n\n");
            text.append("Your catering request #").append(request.getId()).append(" has been updated.\n");
            text.append("Current status: **").append(request.getStatus()).append("**\n\n");

            if ("CONFIRMED".equals(request.getStatus())) {
                text.append("Your request has been confirmed. We'll begin preparations.\n");
                if (request.getTotalQuotedPrice() != null) {
                    text.append("Total amount: Rs. ").append(request.getTotalQuotedPrice()).append("\n");
                }
            } else if ("DELIVERED".equals(request.getStatus())) {
                text.append("Your order has been delivered. We hope you enjoyed it!\n");
                text.append("If you have any feedback, please let us know.\n");
            }

            text.append("\nThank you for choosing Benthota Bake House!");

            message.setText(text.toString());
            mailSender.send(message);
            System.out.println("Catering status update email sent to " + request.getCustomer().getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send catering status email for request #" + request.getId() + ": " + e.getMessage());
        }
    }

    // ===========================
    // Expiration alert email
    // ===========================

    /**
     * Send an email to admin about products and inventory items expiring soon.
     */
    public void sendExpirationAlert(List<Product> products, List<InventoryItem> inventory) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Set recipient(s) – you can change this to your admin email(s)
            message.setTo("admin@benthotabakehouse.com");
            message.setSubject("Expiration Alert – Items Expiring Soon");

            StringBuilder text = new StringBuilder();
            text.append("The following items are expiring within the next 7 days:\n\n");

            if (!products.isEmpty()) {
                text.append("PRODUCTS:\n");
                for (Product p : products) {
                    text.append("- ").append(p.getName()).append(" (expires: ").append(p.getExpirationDate()).append(")\n");
                }
                text.append("\n");
            }

            if (!inventory.isEmpty()) {
                text.append("INVENTORY ITEMS:\n");
                for (InventoryItem i : inventory) {
                    text.append("- ").append(i.getItemName()).append(" (expires: ").append(i.getExpirationDate()).append(")\n");
                }
            }

            message.setText(text.toString());
            mailSender.send(message);
            System.out.println("Expiration alert email sent.");
        } catch (Exception e) {
            System.err.println("Failed to send expiration alert: " + e.getMessage());
        }
    }
}