package com.bakery.system.controller;

import com.bakery.system.entity.Product;
import com.bakery.system.entity.Review;
import com.bakery.system.entity.User;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.UserRepository;
import com.bakery.system.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProductDetailController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    public ProductDetailController(ProductRepository productRepository,
                                   UserRepository userRepository,
                                   ReviewService reviewService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Long id,
                              Model model,
                              Principal principal) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getApprovedReviewsForProduct(product));

        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName());
            boolean hasReviewed = reviewService.hasUserReviewedProduct(product, user);
            model.addAttribute("hasReviewed", hasReviewed);
        } else {
            model.addAttribute("hasReviewed", true); // not logged in, cannot review
        }
        return "product-detail";
    }

    @PostMapping("/product/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam("rating") Integer rating,
                               @RequestParam("comment") String comment,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to submit a review.");
            return "redirect:/login";
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User user = userRepository.findByUsername(principal.getName());

        // Check if user already reviewed this product
        if (reviewService.hasUserReviewedProduct(product, user)) {
            redirectAttributes.addFlashAttribute("error", "You have already reviewed this product.");
            return "redirect:/product/" + id;
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        // status is set to APPROVED in ReviewService
        reviewService.saveReview(review);

        redirectAttributes.addFlashAttribute("success", "Your review has been posted. Thank you!");
        return "redirect:/product/" + id;
    }
}