package com.bakery.system.service;

import com.bakery.system.entity.*;
import com.bakery.system.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class DeliveryAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryAssignmentService.class);

    private final DeliveryPersonnelRepository personnelRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final CateringRequestRepository cateringRequestRepository;

    public DeliveryAssignmentService(DeliveryPersonnelRepository personnelRepository,
                                     DeliveryAssignmentRepository assignmentRepository,
                                     OrderRepository orderRepository,
                                     CateringRequestRepository cateringRequestRepository) {
        this.personnelRepository = personnelRepository;
        this.assignmentRepository = assignmentRepository;
        this.orderRepository = orderRepository;
        this.cateringRequestRepository = cateringRequestRepository;
    }

    @Transactional
    public DeliveryAssignment assignOrderToPersonnel(Long orderId, Long personnelId, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        DeliveryPersonnel personnel = personnelRepository.findById(personnelId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery personnel not found"));

        if (!"AVAILABLE".equals(personnel.getStatus())) {
            throw new IllegalStateException("Delivery personnel is not available");
        }

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setDeliveryPersonnel(personnel);
        assignment.setOrder(order);
        assignment.setAssignmentType("ORDER_DELIVERY");
        assignment.setPickupAddress("Benthota Bake House, 123 Main Street, Colombo");
        assignment.setDeliveryAddress(order.getDeliveryAddress());
        assignment.setCustomerName(order.getUser().getFullName());
        assignment.setCustomerPhone(order.getUser().getTelephoneNumber());
        assignment.setTotalAmount(order.getTotalAmount());
        assignment.setNotes(notes);
        assignment.setStatus("ASSIGNED");

        // Update personnel status
        personnel.setStatus("BUSY");
        personnelRepository.save(personnel);

        // Update order status
        order.setStatus("PROCESSING");
        orderRepository.save(order);

        DeliveryAssignment saved = assignmentRepository.save(assignment);
        logger.info("✅ Order #{} assigned to delivery personnel: {}", orderId, personnel.getName());

        return saved;
    }

    @Transactional
    public DeliveryAssignment assignCateringToPersonnel(Long cateringId, Long personnelId, String notes) {
        CateringRequest catering = cateringRequestRepository.findById(cateringId)
                .orElseThrow(() -> new IllegalArgumentException("Catering request not found"));

        DeliveryPersonnel personnel = personnelRepository.findById(personnelId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery personnel not found"));

        if (!"AVAILABLE".equals(personnel.getStatus())) {
            throw new IllegalStateException("Delivery personnel is not available");
        }

        // Check if catering needs a truck (large order)
        boolean needsTruck = catering.getNumberOfGuests() > 50;
        if (needsTruck && !"Truck".equals(personnel.getVehicleType())) {
            throw new IllegalStateException("This catering order requires a truck due to large quantity");
        }

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setDeliveryPersonnel(personnel);
        assignment.setCateringRequest(catering);
        assignment.setAssignmentType("CATERING_DELIVERY");
        assignment.setPickupAddress("Benthota Bake House, 123 Main Street, Colombo");
        assignment.setDeliveryAddress(catering.getLocation());
        assignment.setCustomerName(catering.getCustomer().getFullName());
        assignment.setCustomerPhone(catering.getCustomer().getTelephoneNumber());
        assignment.setTotalAmount(catering.getTotalQuotedPrice() != null ?
                catering.getTotalQuotedPrice().doubleValue() : 0.0);
        assignment.setNotes(notes);
        assignment.setStatus("ASSIGNED");

        // Update personnel status
        personnel.setStatus("BUSY");
        personnelRepository.save(personnel);

        // Update catering status
        catering.setStatus("PREPARING");
        cateringRequestRepository.save(catering);

        DeliveryAssignment saved = assignmentRepository.save(assignment);
        logger.info("✅ Catering Request #{} assigned to delivery personnel: {} (Truck needed: {})",
                cateringId, personnel.getName(), needsTruck);

        return saved;
    }

    @Transactional
    public DeliveryAssignment updateAssignmentStatus(Long assignmentId, String status) {
        DeliveryAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        assignment.setStatus(status);

        if ("PICKED_UP".equals(status)) {
            assignment.setPickedUpAt(LocalDateTime.now());
            logger.info("📦 Delivery #{} picked up by {}", assignmentId, assignment.getDeliveryPersonnel().getName());
        }

        if ("DELIVERED".equals(status)) {
            assignment.setDeliveredAt(LocalDateTime.now());

            // Update delivery personnel stats
            DeliveryPersonnel personnel = assignment.getDeliveryPersonnel();
            personnel.setCompletedDeliveries(personnel.getCompletedDeliveries() + 1);
            personnel.setStatus("AVAILABLE");
            personnelRepository.save(personnel);

            // Update order or catering status
            if (assignment.getOrder() != null) {
                Order order = assignment.getOrder();
                order.setStatus("DELIVERED");
                orderRepository.save(order);
            }

            if (assignment.getCateringRequest() != null) {
                CateringRequest catering = assignment.getCateringRequest();
                catering.setStatus("DELIVERED");
                cateringRequestRepository.save(catering);
            }

            logger.info("🎉 Delivery #{} completed by {}", assignmentId, personnel.getName());
        }

        return assignmentRepository.save(assignment);
    }

    public List<DeliveryPersonnel> getAvailablePersonnel() {
        return personnelRepository.findByStatus("AVAILABLE");
    }

    public List<DeliveryAssignment> getActiveAssignments() {
        return assignmentRepository.findActiveAssignments();
    }

    public List<DeliveryAssignment> getPersonnelAssignments(Long personnelId) {
        DeliveryPersonnel personnel = personnelRepository.findById(personnelId)
                .orElseThrow(() -> new IllegalArgumentException("Personnel not found"));
        return assignmentRepository.findByDeliveryPersonnel(personnel);
    }

    public DeliveryPersonnel getBestAvailablePersonnel() {
        List<DeliveryPersonnel> available = personnelRepository.findAvailableSortedByWorkload();
        return available.isEmpty() ? null : available.get(0);
    }
}