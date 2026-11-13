package com.bakery.system.service;

import com.bakery.system.entity.User;
import com.bakery.system.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Find user by username
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // 2. Safely get the role and handle NULL values for old database records
        String userRole = user.getRole();
        if (userRole == null || userRole.trim().isEmpty()) {
            userRole = "ROLE_CUSTOMER"; // Default fallback so the app doesn't crash
        }

        // 3. Convert the string role into a Spring Security Authority
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(userRole)
        );

        // 4. Return the fully populated Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}