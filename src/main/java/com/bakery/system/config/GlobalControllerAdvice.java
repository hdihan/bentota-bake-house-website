package com.bakery.system.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("role")
    public String addRoleToModel(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "customer"; // default role for non-logged-in pages
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String auth = authority.getAuthority();
            if ("ROLE_SUPER_ADMIN".equals(auth)) return "superadmin";
            if ("ROLE_ADMIN".equals(auth)) return "staff";  // manager gets staff theme
            if ("ROLE_STAFF".equals(auth)) return "staff";
            if ("ROLE_DRIVER".equals(auth)) return "staff";  // driver gets staff theme
        }
        return "customer";
    }
}