package com.bakery.system.controller;

import com.bakery.system.entity.Promotion;
import com.bakery.system.repository.PromotionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PromotionController {

    private final PromotionRepository promotionRepository;

    public PromotionController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @GetMapping("/promotions")
    public String listPromotions(Model model) {
        model.addAttribute("promotions", promotionRepository.findAll());
        return "promotions";
    }

    @PostMapping("/add-promotion")
    public String addPromotion(@ModelAttribute Promotion promotion,
                               RedirectAttributes redirectAttributes) {
        promotionRepository.save(promotion);
        redirectAttributes.addAttribute("success", true);
        return "redirect:/promotions";
    }

    // 🟢 UPDATE PROMOTION
    @PostMapping("/edit-promotion/{id}")
    public String editPromotion(@PathVariable Long id,
                                @ModelAttribute Promotion promotion,
                                RedirectAttributes redirectAttributes) {
        promotion.setId(id);
        promotionRepository.save(promotion);
        redirectAttributes.addAttribute("updated", true);
        return "redirect:/promotions";
    }

    @GetMapping("/delete-promotion/{id}")
    public String deletePromotion(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        promotionRepository.deleteById(id);
        redirectAttributes.addAttribute("deleted", true);
        return "redirect:/promotions";
    }
}