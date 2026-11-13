package com.bakery.system.service;

import com.bakery.system.entity.Product;
import com.bakery.system.entity.Review;
import com.bakery.system.entity.User;               // <-- ADD THIS IMPORT
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void saveReview(Review review) {
        review.setStatus("APPROVED"); // auto-approve
        reviewRepository.save(review);
        updateProductAverageRating(review.getProduct());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductAverageRating(product);
    }

    public void updateProductAverageRating(Product product) {
        List<Review> approvedReviews = reviewRepository.findByProductAndStatus(product, "APPROVED");
        double avg = approvedReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        product.setAverageRating(avg);
        productRepository.save(product);
    }

    // ✅ Corrected method signatures
    public List<Review> getApprovedReviewsForProduct(Product product) {
        return reviewRepository.findByProductAndStatus(product, "APPROVED");
    }

    public boolean hasUserReviewedProduct(Product product, User user) {
        return reviewRepository.existsByProductAndUserAndStatus(product, user, "APPROVED");
    }
}