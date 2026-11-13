package com.bakery.system.controller;

import com.bakery.system.entity.User;
import com.bakery.system.entity.Product;
import com.bakery.system.entity.InventoryItem;
import com.bakery.system.entity.Order;
import com.bakery.system.entity.CateringRequest;
import com.bakery.system.repository.UserRepository;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.InventoryItemRepository;
import com.bakery.system.repository.CateringRequestRepository;
import com.bakery.system.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final CateringRequestRepository cateringRequestRepository;
    private final OrderRepository orderRepository;

    public AdminController(UserRepository userRepository,
                           ProductRepository productRepository,
                           InventoryItemRepository inventoryItemRepository,
                           PasswordEncoder passwordEncoder,
                           CateringRequestRepository cateringRequestRepository,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.cateringRequestRepository = cateringRequestRepository;
        this.orderRepository = orderRepository;
    }

    // ========== SUPER ADMIN DASHBOARD ==========

    @GetMapping("/super-admin/dashboard")
    public String superAdminDashboard(Model model) {
        List<User> allUsers = userRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        List<CateringRequest> allCatering = cateringRequestRepository.findAll();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("totalProducts", allProducts.size());
        model.addAttribute("activeCatering",
                allCatering.stream().filter(r -> !"DELIVERED".equals(r.getStatus())
                        && !"COMPLETED".equals(r.getStatus())
                        && !"CANCELLED".equals(r.getStatus())).count());

        // Get recent users (last 5)
        model.addAttribute("recentUsers", allUsers.stream()
                .sorted((a,b) -> b.getId().compareTo(a.getId()))
                .limit(5)
                .collect(Collectors.toList()));

        // Get recent orders (last 5)
        model.addAttribute("recentOrders", allOrders.stream()
                .sorted((a,b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .limit(5)
                .collect(Collectors.toList()));

        return "super-admin-dashboard";
    }

    // ========== USER MANAGEMENT ==========

    @GetMapping("/admin/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("newUser", new User());
        return "user-management";
    }

    @PostMapping("/admin/add-user")
    public String addUser(@Valid @ModelAttribute("newUser") User user,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors.");
            return "redirect:/admin/users";
        }

        if (userRepository.findByUsername(user.getUsername()) != null) {
            redirectAttributes.addFlashAttribute("error", "Username already exists!");
            return "redirect:/admin/users";
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            redirectAttributes.addFlashAttribute("error", "Email already registered!");
            return "redirect:/admin/users";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(user.getRole());
        user.setSecurityAnswer(user.getSecurityAnswer() != null ? user.getSecurityAnswer().trim().toLowerCase() : null);
        userRepository.save(user);
        return "redirect:/admin/users?success";
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@RequestParam("id") Long id,
                             @ModelAttribute User user,
                             @RequestParam(value = "newPassword", required = false) String newPassword,
                             RedirectAttributes redirectAttributes) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        // Get current logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername);

        // PREVENT changing SUPER_ADMIN role
        if ("ROLE_SUPER_ADMIN".equals(existingUser.getRole())) {
            // If current user is not the SUPER_ADMIN themselves, deny
            if (!currentUser.getId().equals(existingUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Cannot modify SUPER ADMIN account!");
                return "redirect:/admin/users";
            }
            // If SUPER_ADMIN is editing themselves, prevent role change
            if (!user.getRole().equals("ROLE_SUPER_ADMIN")) {
                redirectAttributes.addFlashAttribute("error", "SUPER ADMIN cannot change their own role!");
                return "redirect:/admin/users";
            }
        }

        User userWithSameUsername = userRepository.findByUsername(user.getUsername());
        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Username already taken by another user!");
            return "redirect:/admin/users";
        }

        User userWithSameEmail = userRepository.findByEmail(user.getEmail());
        if (userWithSameEmail != null && !userWithSameEmail.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Email already registered to another user!");
            return "redirect:/admin/users";
        }

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFullName(user.getFullName());
        existingUser.setHomeAddress(user.getHomeAddress());
        existingUser.setTelephoneNumber(user.getTelephoneNumber());

        // Only update role if not SUPER_ADMIN or if it's the same SUPER_ADMIN keeping the role
        if (!"ROLE_SUPER_ADMIN".equals(existingUser.getRole()) ||
                ("ROLE_SUPER_ADMIN".equals(existingUser.getRole()) && user.getRole().equals("ROLE_SUPER_ADMIN"))) {
            existingUser.setRole(user.getRole());
        }

        existingUser.setSecurityQuestion(user.getSecurityQuestion());
        existingUser.setSecurityAnswer(user.getSecurityAnswer() != null ? user.getSecurityAnswer().trim().toLowerCase() : null);

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(existingUser);
        return "redirect:/admin/users?updated";
    }

    @GetMapping("/admin/delete-user/{id}")
    public String deleteUser(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes) {
        // Get current logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        // Prevent deleting SUPER_ADMIN
        if ("ROLE_SUPER_ADMIN".equals(userToDelete.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete SUPER ADMIN account!");
            return "redirect:/admin/users";
        }

        // Prevent self-deletion
        if (userToDelete.getUsername().equals(currentUsername)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
            return "redirect:/admin/users";
        }

        boolean hasCateringRequests = cateringRequestRepository.existsByCustomer(userToDelete);
        if (hasCateringRequests) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete user because they have existing catering requests. Please delete those requests first.");
            return "redirect:/admin/users";
        }

        userRepository.deleteById(id);
        redirectAttributes.addAttribute("deleted", true);
        return "redirect:/admin/users";
    }

    // ========== INVENTORY MANAGEMENT ==========

    @GetMapping("/inventory")
    public String showInventory(Model model) {
        model.addAttribute("kitchenItems", inventoryItemRepository.findAll());
        model.addAttribute("products", productRepository.findAll());

        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(7);
        List<InventoryItem> expiringInventory = inventoryItemRepository.findByExpirationDateBetween(today, threshold);
        model.addAttribute("expiringInventory", expiringInventory);

        return "inventory";
    }

    @GetMapping("/add-kitchen-item")
    public String showAddKitchenItemForm(Model model) {
        model.addAttribute("kitchenItem", new InventoryItem());
        return "add-kitchen-item";
    }

    @PostMapping("/add-kitchen-item")
    public String addKitchenItem(@ModelAttribute InventoryItem item,
                                 RedirectAttributes redirectAttributes) {
        if (item.getQuantity() != null && item.getQuantity() < 0) {
            redirectAttributes.addFlashAttribute("error", "Quantity cannot be negative!");
            return "redirect:/add-kitchen-item";
        }

        item.setLastRestocked(LocalDate.now());
        inventoryItemRepository.save(item);
        redirectAttributes.addAttribute("success", true);
        return "redirect:/inventory";
    }

    @PostMapping("/edit-kitchen-item/{id}")
    public String editKitchenItem(@PathVariable Long id,
                                  @ModelAttribute InventoryItem item,
                                  RedirectAttributes redirectAttributes) {
        if (item.getQuantity() != null && item.getQuantity() < 0) {
            redirectAttributes.addFlashAttribute("error", "Quantity cannot be negative!");
            return "redirect:/inventory";
        }

        item.setId(id);
        inventoryItemRepository.save(item);
        redirectAttributes.addAttribute("updated", true);
        return "redirect:/inventory";
    }

    @GetMapping("/delete-kitchen-item/{id}")
    public String deleteKitchenItem(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        inventoryItemRepository.deleteById(id);
        redirectAttributes.addAttribute("deleted", true);
        return "redirect:/inventory";
    }

    @PostMapping("/update-stock/{id}")
    public String updateProductStock(@PathVariable Long id,
                                     @RequestParam("newStock") Integer newStock,
                                     RedirectAttributes redirectAttributes) {
        if (newStock != null && newStock < 0) {
            redirectAttributes.addFlashAttribute("error", "Stock quantity cannot be negative!");
            return "redirect:/inventory";
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setStockQuantity(newStock);
        productRepository.save(product);
        redirectAttributes.addAttribute("stockUpdated", true);
        return "redirect:/inventory";
    }
}