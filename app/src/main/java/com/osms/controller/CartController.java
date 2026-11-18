package com.osms.controller;

import com.osms.model.CartItemView;
import com.osms.model.SessionUser;
import com.osms.service.CartException;
import com.osms.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/shop/cart")
public class CartController {

    private static final String REDIRECT_TO_CART = "redirect:/shop/cart";

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String cart(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        List<CartItemView> items = cartService.getItems(user.getCustomerId());
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.getTotal(items));
        return "shop/cart";
    }

    @PostMapping("/add")
    public String add(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                      @RequestParam int productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      RedirectAttributes redirectAttributes) {
        try {
            String productName = cartService.addItem(user.getCustomerId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", productName + " added to your cart");
        } catch (CartException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_TO_CART;
    }

    @PostMapping("/update")
    public String update(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                         @RequestParam int cartItemId,
                         @RequestParam int quantity,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(user.getCustomerId(), cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated");
        } catch (CartException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_TO_CART;
    }

    @PostMapping("/remove")
    public String remove(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                         @RequestParam int cartItemId,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(user.getCustomerId(), cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from your cart");
        } catch (CartException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_TO_CART;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String invalidValue(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid quantity");
        return REDIRECT_TO_CART;
    }
}
