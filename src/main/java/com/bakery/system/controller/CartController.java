package com.bakery.system.controller;

import com.bakery.system.entity.Product;
import com.bakery.system.repository.ProductRepository;
import com.bakery.system.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;

    public CartController(CartService cartService, ProductRepository productRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        Map<Product, Integer> itemsWithProducts = new HashMap<>();
        double total = 0.0;

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product != null) {
                itemsWithProducts.put(product, entry.getValue());
                total += product.getPrice() * entry.getValue();
            }
        }

        model.addAttribute("items", itemsWithProducts);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        cartService.addToCart(session, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/update/{productId}")
    public String updateCart(@PathVariable Long productId,
                             @RequestParam int quantity,
                             HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpSession session) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }
}