package com.bakery.system.controller;

import com.bakery.system.entity.Review;
import com.bakery.system.repository.ReviewRepository;
import com.bakery.system.service.ReviewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff/reviews")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StaffReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public StaffReviewController(ReviewRepository reviewRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String listReviews(@RequestParam(required = false) String status,
                              Model model) {
        List<Review> reviews;
        if (status != null && !status.isEmpty()) {
            reviews = reviewRepository.findByStatus(status);
        } else {
            reviews = reviewRepository.findAll();
        }
        model.addAttribute("reviews", reviews);
        model.addAttribute("selectedStatus", status);
        return "staff-reviews";
    }

    @GetMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting review: " + e.getMessage());
        }
        return "redirect:/staff/reviews";
    }
}