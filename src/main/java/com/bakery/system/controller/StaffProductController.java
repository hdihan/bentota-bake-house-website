package com.bakery.system.controller;

import com.bakery.system.entity.Product;
import com.bakery.system.repository.CategoryRepository;
import com.bakery.system.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
public class StaffProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final String UPLOAD_DIR = System.getProperty("user.home") + "/bakery-uploads/";

    public StaffProductController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "staff-products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "staff-product-edit";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute Product product,
                                BindingResult result,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors.");
            return "redirect:/staff/products/edit/" + id;
        }

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

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
                redirectAttributes.addFlashAttribute("error", "Failed to upload image.");
                return "redirect:/staff/products/edit/" + id;
            }
        } else {
            product.setImageUrl(existingProduct.getImageUrl());
        }

        product.setId(id);
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/staff/products";
    }
}