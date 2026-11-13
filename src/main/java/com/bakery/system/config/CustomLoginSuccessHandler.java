package com.bakery.system.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectURL = "/home";

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            // SUPER_ADMIN now goes to the dedicated Super Admin Panel
            if (role.equals("ROLE_SUPER_ADMIN")) {
                redirectURL = "/super-admin/dashboard";   // ← changed from /admin/users
                break;
            }
            // ADMIN (Manager) and STAFF both go to /dashboard
            else if (role.equals("ROLE_ADMIN") || role.equals("ROLE_STAFF")) {
                redirectURL = "/dashboard";
                break;
            }
            // DRIVER goes to Driver Dashboard
            else if (role.equals("ROLE_DRIVER")) {
                redirectURL = "/driver/dashboard";
                break;
            }
            // CUSTOMER goes to Home page with login success message
            else if (role.equals("ROLE_CUSTOMER")) {
                redirectURL = "/home?login=success";
                break;
            }
        }

        response.sendRedirect(redirectURL);
    }
}