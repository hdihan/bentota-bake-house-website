package com.bakery.system.controller;

import com.bakery.system.entity.Product;
import com.bakery.system.entity.User;
import com.bakery.system.repository.CategoryRepository;
import com.bakery.system.repository.CateringRequestItemRepository;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.PromotionRepository;
import com.bakery.system.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final CateringRequestItemRepository cateringRequestItemRepository;
    private final UserRepository userRepository;

    private final String UPLOAD_DIR = System.getProperty("user.home") + "/bakery-uploads/";

    public ProductController(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             PromotionRepository promotionRepository,
                             CateringRequestItemRepository cateringRequestItemRepository,
                             UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.promotionRepository = promotionRepository;
        this.cateringRequestItemRepository = cateringRequestItemRepository;
        this.userRepository = userRepository;
    }

    // ========== DASHBOARD (Existing) ==========
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        String userRole = currentUser.getRole();
        boolean isAdmin = "ROLE_ADMIN".equals(userRole) || "ROLE_SUPER_ADMIN".equals(userRole);

        List<Product> allProducts = productRepository.findAll();
        List<Product> lowStockProducts = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() < 5)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(7);
        List<Product> expiringProducts = productRepository.findByExpirationDateBetween(today, threshold);

        model.addAttribute("products", allProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("expiringProducts", expiringProducts);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", !isAdmin);
        model.addAttribute("userRole", userRole.replace("ROLE_", ""));
        model.addAttribute("userName", currentUser.getUsername());

        return "dashboard";
    }

    // ========== ADD PRODUCT (GET) ==========
    @GetMapping("/add-product")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "add-product";
    }

    // ========== ADD PRODUCT (POST) ==========
    @PostMapping("/add-product")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        // Validate manually if needed (category selection)
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            result.rejectValue("category", "error.product", "Please select a category");
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "add-product";
        }

        // Handle file upload
        if (!imageFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String filename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload image. Please try again.");
                return "redirect:/add-product";
            }
        }

        // Default status if not set
        if (product.getStatus() == null || product.getStatus().isEmpty()) {
            product.setStatus("Active");
        }
        // Ensure minimum stock
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        return "redirect:/dashboard";
    }

    // ========== EDIT PRODUCT (GET) ==========
    @GetMapping("/edit-product/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "edit-product";
    }

    // ========== EDIT PRODUCT (POST) ==========
    @PostMapping("/edit-product/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute Product product,
                                BindingResult result,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "edit-product";
        }

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        // Update fields
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategory(product.getCategory());
        existing.setStatus(product.getStatus());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setExpirationDate(product.getExpirationDate());

        // Handle image upload if a new file is provided
        if (!imageFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String filename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                existing.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload new image.");
                return "redirect:/edit-product/" + id;
            }
        }
        // else keep existing image

        productRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/dashboard";
    }

    // ========== DELETE PRODUCT ==========
    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/dashboard";
        }

        // Check if product is used in any catering request items
        boolean usedInCatering = cateringRequestItemRepository.existsByProduct(product);
        if (usedInCatering) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete product because it is used in existing catering requests.");
            return "redirect:/dashboard";
        }

        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        return "redirect:/dashboard";
    }

    // ========== PRODUCT BACKLOG (NEW) ==========
    @GetMapping("/products")
    public String showProductBacklog(Model model) {
        List<Product> allProducts = productRepository.findAll();
        model.addAttribute("products", allProducts);
        return "product-backlog";
    }
}