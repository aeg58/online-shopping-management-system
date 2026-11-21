package com.osms.controller;

import com.osms.model.CartItemView;
import com.osms.model.Money;
import com.osms.model.SessionUser;
import com.osms.service.CartException;
import com.osms.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/shop/cart")
public class CartController {

    private static final String REDIRECT_TO_CART = "redirect:/shop/cart";
    /** Pages the customer may come back to after adding a product: the shop list or a product page. */
    private static final Pattern SHOP_PAGE = Pattern.compile("/shop(/product/\\d+)?(\\?[\\w=&%+.-]*)?");

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
                      @RequestParam(defaultValue = "/shop") String returnTo,
                      RedirectAttributes redirectAttributes) {
        try {
            String productName = cartService.addItem(user.getCustomerId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", productName + " added to your cart");
        } catch (CartException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // The customer stays on the page they were on; only shop pages are accepted
        return "redirect:" + (SHOP_PAGE.matcher(returnTo).matches() ? returnTo : "/shop");
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

    /** Called by the cart page script when a quantity changes, so the totals update without the Update button. */
    @PostMapping(value = "/update", headers = "X-Requested-With=fetch")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateLive(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                                          @RequestParam int cartItemId,
                                                          @RequestParam int quantity) {
        try {
            cartService.updateQuantity(user.getCustomerId(), cartItemId, quantity);
        } catch (CartException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        List<CartItemView> items = cartService.getItems(user.getCustomerId());
        CartItemView item = items.stream().filter(i -> i.cartItemId() == cartItemId).findFirst().orElseThrow();
        return ResponseEntity.ok(Map.of(
                "lineTotal", Money.format(item.lineTotal()),
                "total", Money.format(cartService.getTotal(items))));
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
    public Object invalidValue(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if ("fetch".equals(request.getHeader("X-Requested-With"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid quantity"));
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid quantity");
        return REDIRECT_TO_CART;
    }
}
