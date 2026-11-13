package com.bakery.system.repository;

import com.bakery.system.entity.Product;
import com.bakery.system.entity.Review;
import com.bakery.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductAndStatus(Product product, String status);
    List<Review> findByStatus(String status);
    boolean existsByProductAndUserAndStatus(Product product, User user, String status);

}