package com.osms.controller;

import com.osms.model.ProductForm;
import com.osms.model.SellerProductView;
import com.osms.model.SessionUser;
import com.osms.repository.CategoryRepository;
import com.osms.repository.SellerRepository;
import com.osms.service.DatabaseErrors;
import com.osms.service.ProductImageService;
import com.osms.service.ProductFormException;
import com.osms.service.SellerProductService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/** The SellerID always comes from the session, never from a request parameter. */
@Controller
@RequestMapping("/seller")
public class SellerController {

    private static final String PRODUCT_FORM_VIEW = "seller/product-form";
    private static final String REDIRECT_TO_PRODUCTS = "redirect:/seller/products";

    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final SellerProductService sellerProductService;
    private final ProductImageService productImages;

    public SellerController(SellerRepository sellerRepository, CategoryRepository categoryRepository,
                            SellerProductService sellerProductService, ProductImageService productImages) {
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
        this.sellerProductService = sellerProductService;
        this.productImages = productImages;
    }

    @GetMapping
    public String dashboard(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        int sellerId = user.getSellerId();
        model.addAttribute("productCount", sellerRepository.countProducts(sellerId));
        model.addAttribute("sales", sellerRepository.findSales(sellerId));
        model.addAttribute("lowStock", sellerRepository.findLowStock(sellerId));
        return "seller/dashboard";
    }

    @GetMapping("/products")
    public String products(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        model.addAttribute("products", sellerRepository.findProducts(user.getSellerId()));
        return "seller/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("form", new ProductForm());
        model.addAttribute("categories", categoryRepository.findAll());
        return PRODUCT_FORM_VIEW;
    }

    @PostMapping("/products")
    public String addProduct(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                             @ModelAttribute("form") ProductForm form,
                             @RequestParam(value = "photo", required = false) MultipartFile photo,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            // The photo is checked first, so a wrong file does not leave a product without it
            String photoExtension = productImages.check(photo);
            int productId = sellerProductService.addProduct(user.getSellerId(), form);
            if (photoExtension != null) {
                try {
                    productImages.save(productId, photo, photoExtension);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "The product was added, but the photo could not be saved");
                    return REDIRECT_TO_PRODUCTS;
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Product added");
            return REDIRECT_TO_PRODUCTS;
        } catch (ProductFormException e) {
            model.addAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", DatabaseErrors.message(e));
        }
        model.addAttribute("categories", categoryRepository.findAll());
        return PRODUCT_FORM_VIEW;
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                              @PathVariable int id,
                              Model model) {
        SellerProductView product = findOwnProduct(user, id);
        ProductForm form = new ProductForm();
        form.setDescription(product.description() == null ? "" : product.description());
        form.setPrice(product.price().toPlainString());
        form.setStock(String.valueOf(product.quantityInStock()));
        form.setActive(product.active());

        model.addAttribute("product", product);
        model.addAttribute("form", form);
        return PRODUCT_FORM_VIEW;
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                @PathVariable int id,
                                @ModelAttribute("form") ProductForm form,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        SellerProductView product = findOwnProduct(user, id);
        try {
            sellerProductService.updateProduct(user.getSellerId(), id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated");
            return REDIRECT_TO_PRODUCTS;
        } catch (ProductFormException e) {
            model.addAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", DatabaseErrors.message(e));
        }
        model.addAttribute("product", product);
        return PRODUCT_FORM_VIEW;
    }

    @GetMapping("/orders")
    public String orders(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        model.addAttribute("orderLines", sellerRepository.findOrderLines(user.getSellerId()));
        return "seller/orders";
    }

    private SellerProductView findOwnProduct(SessionUser user, int productId) {
        return sellerRepository.findProduct(user.getSellerId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidValue() {
        return REDIRECT_TO_PRODUCTS;
    }
}
