package com.bakery.system.controller;

import com.bakery.system.entity.*;
import com.bakery.system.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/catering")
public class CateringController {

    private final CateringRequestRepository cateringRequestRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SetMenuRepository setMenuRepository;

    public CateringController(CateringRequestRepository cateringRequestRepository,
                              ProductRepository productRepository,
                              UserRepository userRepository,
                              SetMenuRepository setMenuRepository) {
        this.cateringRequestRepository = cateringRequestRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.setMenuRepository = setMenuRepository;
    }

    // Show form to create a new catering request
    @GetMapping("/request")
    public String showRequestForm(Model model) {
        model.addAttribute("cateringRequest", new CateringRequest());
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("setMenus", setMenuRepository.findByStatus("ACTIVE"));
        return "catering-request-form";
    }

    // Handle form submission (with set menus and modifications)
    @PostMapping("/request")
    public String submitRequest(@Valid @ModelAttribute("cateringRequest") CateringRequest cateringRequest,
                                BindingResult result,
                                @RequestParam(value = "productIds", required = false) List<Long> productIds,
                                @RequestParam(value = "quantities", required = false) List<Integer> quantities,
                                @RequestParam(value = "customItemNames", required = false) List<String> customItemNames,
                                @RequestParam(value = "customDescriptions", required = false) List<String> customDescriptions,
                                @RequestParam(value = "customQuantities", required = false) List<Integer> customQuantities,
                                @RequestParam(value = "customNotes", required = false) List<String> customNotes,
                                @RequestParam(value = "setMenuIds", required = false) List<Long> setMenuIds,
                                @RequestParam(value = "distance", required = false) Double distance,
                                HttpServletRequest request,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors.");
            return "redirect:/catering/request";
        }

        try {
            User customer = userRepository.findByUsername(principal.getName());
            cateringRequest.setCustomer(customer);
            cateringRequest.setStatus("PENDING");
            cateringRequest.setCreatedAt(LocalDateTime.now());

            // Set distance if provided
            if (distance != null && distance > 0) {
                cateringRequest.setDistance(distance);
            }

            boolean hasItems = false;

            // Add items from existing products
            if (productIds != null && quantities != null) {
                for (int i = 0; i < productIds.size(); i++) {
                    Long productId = productIds.get(i);
                    Integer qty = (i < quantities.size()) ? quantities.get(i) : null;
                    if (productId != null && productId > 0 && qty != null && qty > 0) {
                        Product product = productRepository.findById(productId).orElse(null);
                        if (product != null) {
                            CateringRequestItem item = new CateringRequestItem();
                            item.setRequest(cateringRequest);
                            item.setProduct(product);
                            item.setItemName(product.getName());
                            item.setDescription(product.getDescription());
                            item.setQuantity(qty);
                            cateringRequest.getItems().add(item);
                            hasItems = true;
                        }
                    }
                }
            }

            // Add custom items
            if (customItemNames != null && customQuantities != null) {
                for (int i = 0; i < customItemNames.size(); i++) {
                    String name = customItemNames.get(i);
                    Integer qty = (i < customQuantities.size()) ? customQuantities.get(i) : null;
                    if (name != null && !name.trim().isEmpty() && qty != null && qty > 0) {
                        CateringRequestItem item = new CateringRequestItem();
                        item.setRequest(cateringRequest);
                        item.setItemName(name.trim());
                        if (customDescriptions != null && i < customDescriptions.size() && customDescriptions.get(i) != null) {
                            item.setDescription(customDescriptions.get(i));
                        }
                        item.setQuantity(qty);
                        if (customNotes != null && i < customNotes.size() && customNotes.get(i) != null) {
                            item.setNotes(customNotes.get(i));
                        }
                        cateringRequest.getItems().add(item);
                        hasItems = true;
                    }
                }
            }

            // Handle set menus
            if (setMenuIds != null) {
                for (Long menuId : setMenuIds) {
                    SetMenu setMenu = setMenuRepository.findById(menuId).orElse(null);
                    if (setMenu != null) {
                        List<SetMenuItem> menuItems = setMenu.getItems();
                        for (int idx = 0; idx < menuItems.size(); idx++) {
                            SetMenuItem smItem = menuItems.get(idx);
                            CateringRequestItem item = new CateringRequestItem();
                            item.setRequest(cateringRequest);
                            item.setSetMenu(setMenu);
                            if (smItem.getProduct() != null) {
                                item.setProduct(smItem.getProduct());
                            }
                            item.setItemName(smItem.getItemName());
                            item.setDescription(smItem.getDescription());
                            item.setQuantity(smItem.getDefaultQuantity() != null ? smItem.getDefaultQuantity() : 1);

                            String modKey = "modifications_" + menuId + "_" + idx;
                            String[] modValues = request.getParameterValues(modKey);
                            if (modValues != null && modValues.length > 0 && modValues[0] != null && !modValues[0].trim().isEmpty()) {
                                item.setModificationRequest(modValues[0].trim());
                            }

                            cateringRequest.getItems().add(item);
                            hasItems = true;
                        }
                    }
                }
            }

            if (!hasItems) {
                redirectAttributes.addFlashAttribute("error", "Please add at least one item to your request.");
                return "redirect:/catering/request";
            }

            cateringRequestRepository.save(cateringRequest);
            redirectAttributes.addFlashAttribute("success", "Your catering request has been submitted successfully!");
            return "redirect:/catering/my-requests";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            return "redirect:/catering/request";
        }
    }

    // List customer's own requests
    @GetMapping("/my-requests")
    public String myRequests(Model model, Principal principal) {
        User customer = userRepository.findByUsername(principal.getName());
        List<CateringRequest> requests = cateringRequestRepository.findByCustomer(customer);
        model.addAttribute("requests", requests);
        return "catering-my-requests";
    }

    // View a single request details (customer view)
    @GetMapping("/request/{id}")
    public String viewRequest(@PathVariable Long id, Model model, Principal principal) {
        CateringRequest request = cateringRequestRepository.findById(id).orElse(null);
        if (request == null) {
            return "redirect:/catering/my-requests?error=notfound";
        }
        User customer = userRepository.findByUsername(principal.getName());
        if (!request.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/catering/my-requests?error=accessdenied";
        }
        model.addAttribute("request", request);
        return "catering-request-detail";
    }

    // Delete request (customer only, allowed for PENDING/QUOTED)
    @PostMapping("/request/{id}/delete")
    public String deleteRequest(@PathVariable Long id,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        CateringRequest request = cateringRequestRepository.findById(id).orElse(null);
        if (request == null) {
            redirectAttributes.addFlashAttribute("error", "Request not found.");
            return "redirect:/catering/my-requests";
        }

        User currentUser = userRepository.findByUsername(principal.getName());
        if (!request.getCustomer().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this request.");
            return "redirect:/catering/my-requests";
        }

        if (!"PENDING".equals(request.getStatus()) && !"QUOTED".equals(request.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Only pending or quoted requests can be deleted.");
            return "redirect:/catering/my-requests";
        }

        cateringRequestRepository.delete(request);
        redirectAttributes.addFlashAttribute("success", "Catering request deleted successfully.");
        return "redirect:/catering/my-requests";
    }
}