package com.bakery.system.controller;

import com.bakery.system.entity.Category;
import com.bakery.system.repository.CategoryRepository;
import com.bakery.system.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryController(CategoryRepository categoryRepository,
                              ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("totalProducts", productRepository.count());

        Map<String, Long> categoryCounts = new HashMap<>();
        productRepository.findAll().forEach(p -> {
            String catName = p.getCategory() != null ? p.getCategory().getName() : "Uncategorized";
            categoryCounts.put(catName, categoryCounts.getOrDefault(catName, 0L) + 1);
        });
        model.addAttribute("categoryCounts", categoryCounts);
        return "categories";
    }

    @PostMapping("/add-category")
    public String addCategory(@ModelAttribute Category category,
                              RedirectAttributes redirectAttributes) {
        categoryRepository.save(category);
        redirectAttributes.addAttribute("success", true);
        return "redirect:/categories";
    }

    @PostMapping("/edit-category/{id}")
    public String editCategory(@PathVariable Long id,
                               @ModelAttribute Category category,
                               RedirectAttributes redirectAttributes) {
        category.setId(id);
        categoryRepository.save(category);
        redirectAttributes.addAttribute("updated", true);
        return "redirect:/categories";
    }

    @GetMapping("/delete-category/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Category not found.");
            return "redirect:/categories";
        }

        // Check if any product uses this category
        if (!productRepository.findByCategory(category).isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete category because it is used by products. Reassign those products first.");
            return "redirect:/categories";
        }

        categoryRepository.deleteById(id);
        redirectAttributes.addAttribute("deleted", true);
        return "redirect:/categories";
    }
}