package com.bakery.system.service;

import com.bakery.system.entity.Product;
import com.bakery.system.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";
    private final ProductRepository productRepository;

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCartMap(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getCartMap(session);
        cart.merge(productId, quantity, Integer::sum);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getCartMap(session);
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeItem(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCartMap(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public Map<Long, Integer> getCartItems(HttpSession session) {
        return getCartMap(session);
    }

    public double getTotalAmount(HttpSession session) {
        Map<Long, Integer> cart = getCartMap(session);
        double total = 0.0;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product != null) {
                total += product.getPrice() * entry.getValue();
            }
        }
        return total;
    }
}