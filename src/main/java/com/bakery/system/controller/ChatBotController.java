package com.bakery.system.controller;

import com.bakery.system.service.AIChatBotService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Controller
@RequestMapping("/chatbot")
public class ChatBotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatBotController.class);
    private final AIChatBotService aiChatBotService;

    public ChatBotController(AIChatBotService aiChatBotService) {
        this.aiChatBotService = aiChatBotService;
    }

    @GetMapping
    public String chatbotPage() {
        return "chatbot";
    }

    @PostMapping("/ask")
    @ResponseBody
    public Map<String, String> ask(@RequestBody Map<String, String> payload, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        String userMessage = payload.get("message");
        String sessionId = session.getId();

        logger.info("User: {}", userMessage);

        String botReply = aiChatBotService.getResponse(userMessage, sessionId);
        response.put("botReply", botReply);

        return response;
    }
}