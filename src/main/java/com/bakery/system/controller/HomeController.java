package com.bakery.system.controller;

import com.bakery.system.entity.Category;
import com.bakery.system.entity.Product;
import com.bakery.system.entity.User;
import com.bakery.system.repository.CategoryRepository;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.PromotionRepository;
import com.bakery.system.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bakery.system.entity.Promotion;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;

    public HomeController(ProductRepository productRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          PromotionRepository promotionRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.promotionRepository = promotionRepository;
    }

    @GetMapping({"/", "/home"})
    public String showHomePage(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Product> products;

        // Determine which products to show
        if (categoryId != null && categoryId > 0) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
            products = productRepository.findByCategory(cat);
        } else if (search != null && !search.trim().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else {
            products = productRepository.findAll();
        }

        // Fetch active promotions (status = Active and date range includes today)
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(LocalDate.now());
        model.addAttribute("promotions", activePromotions);

        // Add attributes to model
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("searchTerm", search);
        model.addAttribute("promotions", activePromotions);   // for home.html

        return "home";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                @RequestParam("homeAddress") String homeAddress,
                                @RequestParam("telephoneNumber") String telephoneNumber,
                                Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setHomeAddress(homeAddress);
        user.setTelephoneNumber(telephoneNumber);
        userRepository.save(user);
        return "redirect:/profile?updated";
    }
}