package com.bakery.system.controller;

import com.bakery.system.entity.User;
import com.bakery.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "Username already taken.");
            return "register";
        }
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already registered.");
            return "register";
        }

        userService.saveNewUser(user);
        return "redirect:/login?success";
    }

    // ========== FORGOT PASSWORD FLOW ==========

    // Show forgot password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // Process security question verification and generate reset token
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("username") String username,
                                        @RequestParam("securityQuestion") String securityQuestion,
                                        @RequestParam("securityAnswer") String securityAnswer,
                                        RedirectAttributes redirectAttributes) {
        String token = userService.verifySecurityAnswerAndGenerateToken(username, securityQuestion, securityAnswer);
        if (token == null) {
            redirectAttributes.addAttribute("error", "Invalid username or security answer.");
            return "redirect:/forgot-password";
        }
        // Redirect to reset password page with token
        return "redirect:/reset-password?token=" + token;
    }

    // Show reset password form (with token)
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token,
                                        Model model) {
        User user = userService.getByResetToken(token);
        if (user == null) {
            model.addAttribute("error", "Invalid or expired token.");
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    // Process new password
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        User user = userService.getByResetToken(token);
        if (user == null) {
            redirectAttributes.addAttribute("error", "Invalid or expired token.");
            return "redirect:/forgot-password";
        }
        userService.updatePassword(user, password);
        redirectAttributes.addAttribute("reset", "success");
        return "redirect:/login";
    }
}