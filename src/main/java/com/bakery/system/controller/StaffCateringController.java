package com.bakery.system.controller;

import com.bakery.system.entity.CateringRequest;
import com.bakery.system.entity.CateringRequestItem;
import com.bakery.system.entity.DeliverySettings;
import com.bakery.system.repository.CateringRequestRepository;
import com.bakery.system.repository.DeliverySettingsRepository;
import com.bakery.system.service.EmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff/catering")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
public class StaffCateringController {

    private final CateringRequestRepository cateringRequestRepository;
    private final DeliverySettingsRepository deliverySettingsRepository;
    private final EmailService emailService;

    public StaffCateringController(CateringRequestRepository cateringRequestRepository,
                                   DeliverySettingsRepository deliverySettingsRepository,
                                   EmailService emailService) {
        this.cateringRequestRepository = cateringRequestRepository;
        this.deliverySettingsRepository = deliverySettingsRepository;
        this.emailService = emailService;
    }

    @GetMapping("/requests")
    public String listAllRequests(@RequestParam(value = "status", required = false) String status,
                                  Model model) {
        List<CateringRequest> requests;
        if (status != null && !status.isEmpty()) {
            requests = cateringRequestRepository.findByStatus(status);
        } else {
            requests = cateringRequestRepository.findAll();
        }
        model.addAttribute("requests", requests);
        model.addAttribute("selectedStatus", status);
        return "staff-catering-requests";
    }

    @GetMapping("/request/{id}")
    @Transactional
    public String viewRequest(@PathVariable Long id, Model model) {
        CateringRequest request = cateringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request Id:" + id));
        // Force initialize the items collection
        request.getItems().size();
        model.addAttribute("request", request);

        DeliverySettings settings = deliverySettingsRepository.findActive();
        model.addAttribute("deliverySettings", settings);

        return "staff-catering-request-detail";
    }

    @PostMapping("/request/{id}/quote")
    @Transactional
    public String submitQuotation(@PathVariable Long id,
                                  @RequestParam(value = "distance", required = false) Double distance,
                                  @RequestParam(value = "serviceCharge", required = false) BigDecimal serviceCharge,
                                  @RequestParam(value = "totalQuotedPrice", required = false) BigDecimal totalQuotedPrice,
                                  @RequestParam(value = "managerNotes", required = false) String managerNotes,
                                  @RequestParam Map<String, String> allParams,
                                  RedirectAttributes redirectAttributes) {

        CateringRequest request = cateringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request Id:" + id));

        if (distance != null) {
            request.setDistance(distance);
            DeliverySettings settings = deliverySettingsRepository.findActive();
            if (settings != null) {
                if (distance <= settings.getFreeDistanceKm()) {
                    request.setDeliveryFee(0.0);
                } else {
                    request.setDeliveryFee(settings.getAdditionalCharge());
                }
            } else {
                request.setDeliveryFee(0.0);
            }
        }

        request.setServiceCharge(serviceCharge != null ? serviceCharge : BigDecimal.ZERO);

        // Force initialize items to update prices
        request.getItems().size();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CateringRequestItem item : request.getItems()) {
            String priceParam = "itemPrice_" + item.getId();
            if (allParams.containsKey(priceParam)) {
                try {
                    BigDecimal unitPrice = new BigDecimal(allParams.get(priceParam));
                    item.setQuotedUnitPrice(unitPrice);
                    subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        BigDecimal total = subtotal;
        if (request.getServiceCharge() != null) total = total.add(request.getServiceCharge());
        if (request.getDeliveryFee() != null) total = total.add(BigDecimal.valueOf(request.getDeliveryFee()));

        if (totalQuotedPrice != null) {
            request.setTotalQuotedPrice(totalQuotedPrice);
        } else {
            request.setTotalQuotedPrice(total);
        }

        request.setManagerNotes(managerNotes);
        request.setQuotationDate(LocalDateTime.now());
        request.setStatus("QUOTED");
        cateringRequestRepository.save(request);

        emailService.sendCateringQuotation(request);

        redirectAttributes.addFlashAttribute("success", "Quotation submitted and email sent.");
        return "redirect:/staff/catering/request/" + id + "?quoted";
    }

    @PostMapping("/request/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes) {
        CateringRequest request = cateringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request Id:" + id));
        String oldStatus = request.getStatus();
        request.setStatus(status);
        if ("CONFIRMED".equalsIgnoreCase(status)) {
            request.setConfirmationDate(LocalDateTime.now());
        }
        cateringRequestRepository.save(request);

        if (!oldStatus.equals(status) && ("CONFIRMED".equals(status) || "DELIVERED".equals(status))) {
            emailService.sendCateringStatusUpdate(request);
            redirectAttributes.addFlashAttribute("emailSent", "Email sent to customer.");
        }

        redirectAttributes.addFlashAttribute("success", "Status updated to " + status);
        return "redirect:/staff/catering/request/" + id + "?statusUpdated";
    }
}