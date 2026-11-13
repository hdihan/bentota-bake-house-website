package com.bakery.system.repository;

import com.bakery.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    // 🟢 Find by token when they are authorized to reset password
    User findByResetToken(String resetToken);

    User findByEmail(String email);
}