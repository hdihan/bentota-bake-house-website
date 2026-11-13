package com.bakery.system.controller;

import com.bakery.system.entity.SetMenu;
import com.bakery.system.entity.SetMenuItem;
import com.bakery.system.entity.Product;
import com.bakery.system.repository.SetMenuRepository;
import com.bakery.system.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff/setmenus")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
public class SetMenuController {

    private final SetMenuRepository setMenuRepository;
    private final ProductRepository productRepository;

    public SetMenuController(SetMenuRepository setMenuRepository, ProductRepository productRepository) {
        this.setMenuRepository = setMenuRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    @Transactional
    public String listSetMenus(Model model) {
        List<SetMenu> setMenus = setMenuRepository.findAll();
        // Force initialize items for each set menu
        for (SetMenu menu : setMenus) {
            menu.getItems().size();
        }
        model.addAttribute("setMenus", setMenus);
        return "staff-setmenus";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("setMenu", new SetMenu());
        model.addAttribute("products", productRepository.findAll());
        return "setmenu-form";
    }

    @PostMapping("/save")
    @Transactional
    public String saveSetMenu(@Valid @ModelAttribute SetMenu setMenu,
                              BindingResult result,
                              @RequestParam(value = "productIds", required = false) List<Long> productIds,
                              @RequestParam(value = "itemNames", required = false) List<String> itemNames,
                              @RequestParam(value = "descriptions", required = false) List<String> descriptions,
                              @RequestParam(value = "quantities", required = false) List<Integer> quantities,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors.");
            return "redirect:/staff/setmenus/new";
        }

        SetMenu saved = setMenuRepository.save(setMenu);
        saved.getItems().clear();

        if (productIds != null) {
            for (int i = 0; i < productIds.size(); i++) {
                Long pid = productIds.get(i);
                if (pid != null && pid > 0) {
                    Product product = productRepository.findById(pid).orElse(null);
                    if (product != null) {
                        SetMenuItem item = new SetMenuItem();
                        item.setSetMenu(saved);
                        item.setProduct(product);
                        item.setItemName(product.getName());
                        item.setDescription(product.getDescription());
                        item.setDefaultQuantity(quantities != null && i < quantities.size() ? quantities.get(i) : 1);
                        saved.getItems().add(item);
                    }
                }
            }
        }

        if (itemNames != null) {
            for (int i = 0; i < itemNames.size(); i++) {
                String name = itemNames.get(i);
                if (name != null && !name.trim().isEmpty()) {
                    SetMenuItem item = new SetMenuItem();
                    item.setSetMenu(saved);
                    item.setItemName(name);
                    if (descriptions != null && i < descriptions.size()) {
                        item.setDescription(descriptions.get(i));
                    }
                    item.setDefaultQuantity(quantities != null && i < quantities.size() ? quantities.get(i) : 1);
                    saved.getItems().add(item);
                }
            }
        }

        setMenuRepository.save(saved);
        return "redirect:/staff/setmenus?success";
    }

    @GetMapping("/edit/{id}")
    @Transactional
    public String showEditForm(@PathVariable Long id, Model model) {
        SetMenu setMenu = setMenuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set menu Id:" + id));
        // Force initialize items
        setMenu.getItems().size();
        model.addAttribute("setMenu", setMenu);
        model.addAttribute("products", productRepository.findAll());
        return "setmenu-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteSetMenu(@PathVariable Long id) {
        setMenuRepository.deleteById(id);
        return "redirect:/staff/setmenus?deleted";
    }
}