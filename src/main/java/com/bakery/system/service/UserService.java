package com.bakery.system.service;

import com.bakery.system.entity.User;
import com.bakery.system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🟢 NEW: Check if Username already exists
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    // 🟢 NEW: Check if Email already exists
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public void saveNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getSecurityAnswer() != null) {
            user.setSecurityAnswer(user.getSecurityAnswer().trim().toLowerCase());
        }
        user.setRole("ROLE_CUSTOMER");
        userRepository.save(user);
    }

    // ... (Keep the rest of your Forgot Password methods exactly the same) ...
    public String verifySecurityAnswerAndGenerateToken(String username, String question, String answer) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getSecurityQuestion() != null) {
            if (user.getSecurityQuestion().equals(question) &&
                    user.getSecurityAnswer().equals(answer.trim().toLowerCase())) {
                String token = UUID.randomUUID().toString();
                user.setResetToken(token);
                userRepository.save(user);
                return token;
            }
        }
        return null;
    }

    public User getByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }
}