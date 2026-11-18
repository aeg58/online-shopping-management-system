package com.osms.controller;

import com.osms.model.ProductView;
import com.osms.model.SessionUser;
import com.osms.repository.CategoryRepository;
import com.osms.repository.ProductRepository;
import com.osms.repository.ReviewRepository;
import com.osms.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ShopController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public ShopController(ProductRepository productRepository, CategoryRepository categoryRepository,
                          ReviewRepository reviewRepository, ReviewService reviewService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(defaultValue = "0") int category,
                       @RequestParam(defaultValue = "") String q,
                       Model model) {
        String search = q.trim();
        model.addAttribute("products", productRepository.findActive(category, search));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search);
        return "shop/index";
    }

    @GetMapping("/shop/product/{id}")
    public String product(@PathVariable int id,
                          @ModelAttribute("currentUser") SessionUser currentUser,
                          Model model) {
        ProductView product = productRepository.findActiveById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean isCustomer = currentUser != null && SessionUser.ROLE_CUSTOMER.equals(currentUser.getRoleName());

        model.addAttribute("product", product);
        model.addAttribute("rating", reviewRepository.findRatingSummary(id));
        model.addAttribute("reviews", reviewRepository.findByProduct(id));
        model.addAttribute("canReview", isCustomer && reviewService.canReview(currentUser.getCustomerId(), id));
        return "shop/product";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidValue() {
        return "redirect:/shop";
    }
}
