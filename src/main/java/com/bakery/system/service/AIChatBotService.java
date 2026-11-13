package com.bakery.system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bakery.system.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIChatBotService {

    private static final Logger logger = LoggerFactory.getLogger(AIChatBotService.class);

    @Value("${openrouter.api.key:}")
    private String openRouterKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductRepository productRepository;

    public AIChatBotService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String getResponse(String userMessage, String sessionId) {
        if (openRouterKey == null || openRouterKey.isEmpty()) {
            logger.error("NO API KEY! Get one from https://openrouter.ai/");
            return getFallbackResponse(userMessage);
        }

        return callOpenRouterAPI(userMessage);
    }

    private String callOpenRouterAPI(String userMessage) {
        try {
            String url = "https://openrouter.ai/api/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openRouterKey);
            headers.set("HTTP-Referer", "http://localhost:8080");
            headers.set("X-Title", "Benthota Bake House Chatbot");

            // Get products from database
            var products = productRepository.findAll();
            StringBuilder productList = new StringBuilder();
            for (var product : products) {
                productList.append("- ").append(product.getName())
                        .append(": Rs. ").append(product.getPrice()).append("\n");
            }

            // Build messages
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "You are BakeBot, a friendly AI assistant for Benthota Bake House bakery in Sri Lanka.\n\n" +
                            "LOCATION: 123 Main Street, Colombo (near railway station)\n" +
                            "HOURS: 8:00 AM to 8:00 PM daily\n" +
                            "PHONE: +94 77 123 4567\n" +
                            "EMAIL: info@benthotabakehouse.com\n" +
                            "CATERING MANAGER: jonathan@benthotabakehouse.com, Phone: 071 564 8978\n\n" +
                            "PRODUCTS:\n" + productList.toString() + "\n" +
                            "DELIVERY: Free within 5km for orders over Rs. 2000\n\n" +
                            "PAYMENT: Cash on Delivery, Credit/Debit Cards, Online Bank Transfers\n\n" +
                            "RULES:\n" +
                            "- Be warm, friendly, and conversational\n" +
                            "- Use emojis naturally 🍰🥐🍞\n" +
                            "- Keep responses to 2-3 sentences\n" +
                            "- If asked for catering contact, give jonathan@benthotabakehouse.com and 071 564 8978\n" +
                            "- If asked for location, give 123 Main Street, Colombo\n" +
                            "- Recommend specific products from the list above");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "google/gemini-2.0-flash-exp:free");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.8);
            requestBody.put("max_tokens", 300);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            logger.info("🤖 Calling OpenRouter API for: {}", userMessage);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            logger.info("📡 Response status: {}", response.getStatusCode());

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("error")) {
                String error = jsonNode.path("error").path("message").asText();
                logger.error("API Error: {}", error);
                return getFallbackResponse(userMessage);
            }

            String aiResponse = jsonNode.path("choices").get(0).path("message").path("content").asText();
            logger.info("✅ AI: {}", aiResponse);
            return aiResponse;

        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            return getFallbackResponse(userMessage);
        }
    }

    private String getFallbackResponse(String message) {
        String msg = message.toLowerCase().trim();

        if (msg.contains("hungry") || msg.contains("eat") || msg.contains("food")) {
            return "🍽️ Try our Chicken Roll (Rs. 120) - it's our bestseller! Or Chocolate Cake slice (Rs. 250) for something sweet!";
        }
        if (msg.contains("roll")) {
            return "🥐 We have Chicken Roll (Rs. 120), Cheese Roll (Rs. 100), and Vegetable Roll (Rs. 80). The Chicken Roll is most popular!";
        }
        if (msg.contains("cake")) {
            return "🎂 Chocolate Cake (Rs. 250/slice), Red Velvet (Rs. 220), and custom cakes from Rs. 1500!";
        }
        if (msg.contains("catering")) {
            return "🎉 Contact Jonathan at jonathan@benthotabakehouse.com or 071 564 8978 for catering!";
        }
        if (msg.contains("location") || msg.contains("where")) {
            return "📍 We're at 123 Main Street, Colombo - near the railway station!";
        }
        if (msg.contains("delivery")) {
            return "🚚 Free delivery within 5km for orders over Rs. 2000!";
        }
        if (msg.contains("hour") || msg.contains("open")) {
            return "🕒 Open daily 8 AM to 8 PM!";
        }
        if (msg.contains("payment")) {
            return "💳 We accept Cash on Delivery, Credit/Debit Cards, and Online Bank Transfers!";
        }
        if (msg.contains("hello") || msg.contains("hi")) {
            return "👋 Hello! Welcome to Benthota Bake House! Ask me about our rolls, cakes, delivery, or catering!";
        }

        return "🍰 Welcome to Benthota Bake House! You can ask me about:\n• Food (rolls, cakes, breads)\n• Delivery\n• Catering\n• Location & hours\n• Payment\n\nWhat would you like to know?";
    }
}