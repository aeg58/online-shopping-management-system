package com.osms.controller;

import com.osms.model.Category;
import com.osms.model.CouponForm;
import com.osms.repository.CategoryRepository;
import com.osms.repository.CouponRepository;
import com.osms.service.AdminRequestException;
import com.osms.service.AdminService;
import com.osms.service.DatabaseErrors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminCatalogController {

    private static final String CATEGORY_FORM_VIEW = "admin/category-form";
    private static final String COUPON_FORM_VIEW = "admin/coupon-form";

    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final AdminService adminService;

    public AdminCatalogController(CategoryRepository categoryRepository, CouponRepository couponRepository,
                                  AdminService adminService) {
        this.categoryRepository = categoryRepository;
        this.couponRepository = couponRepository;
        this.adminService = adminService;
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryName", "");
        model.addAttribute("description", "");
        return CATEGORY_FORM_VIEW;
    }

    @PostMapping("/categories")
    public String addCategory(@RequestParam(defaultValue = "") String categoryName,
                              @RequestParam(defaultValue = "") String description,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            adminService.addCategory(categoryName, description);
            redirectAttributes.addFlashAttribute("successMessage", "Category added");
            return "redirect:/admin/categories";
        } catch (AdminRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", DatabaseErrors.readableMessage(e));
        }
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("description", description);
        return CATEGORY_FORM_VIEW;
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable int id, Model model) {
        Category category = findCategory(id);
        model.addAttribute("category", category);
        model.addAttribute("categoryName", category.categoryName());
        model.addAttribute("description", category.description() == null ? "" : category.description());
        return CATEGORY_FORM_VIEW;
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable int id,
                                 @RequestParam(defaultValue = "") String categoryName,
                                 @RequestParam(defaultValue = "") String description,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Category category = findCategory(id);
        try {
            adminService.updateCategory(id, categoryName, description);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated");
            return "redirect:/admin/categories";
        } catch (AdminRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", DatabaseErrors.readableMessage(e));
        }
        model.addAttribute("category", category);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("description", description);
        return CATEGORY_FORM_VIEW;
    }

    @GetMapping("/coupons")
    public String coupons(Model model) {
        model.addAttribute("coupons", couponRepository.findAll());
        return "admin/coupons";
    }

    @GetMapping("/coupons/new")
    public String newCoupon(Model model) {
        model.addAttribute("form", new CouponForm());
        return COUPON_FORM_VIEW;
    }

    @PostMapping("/coupons")
    public String addCoupon(@ModelAttribute("form") CouponForm form,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            adminService.addCoupon(form);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon added");
            return "redirect:/admin/coupons";
        } catch (AdminRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", DatabaseErrors.readableMessage(e));
        }
        return COUPON_FORM_VIEW;
    }

    private Category findCategory(int categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
