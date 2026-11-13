package com.bakery.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuperAdminController {

    @GetMapping("/super-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String superAdminRedirect() {
        return "redirect:/super-admin/dashboard";
    }
}