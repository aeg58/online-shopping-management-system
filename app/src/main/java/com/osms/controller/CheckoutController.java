package com.osms.controller;

import com.osms.model.CartItemView;
import com.osms.model.SessionUser;
import com.osms.repository.AddressRepository;
import com.osms.service.CartService;
import com.osms.service.CheckoutException;
import com.osms.service.DatabaseErrors;
import com.osms.service.OrderService;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/shop/checkout")
public class CheckoutController {

    private static final String REDIRECT_TO_CHECKOUT = "redirect:/shop/checkout";

    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, AddressRepository addressRepository,
                              OrderService orderService) {
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public String checkout(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        List<CartItemView> items = cartService.getItems(user.getCustomerId());
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty");
            return "redirect:/shop/cart";
        }
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.getTotal(items));
        model.addAttribute("addresses", addressRepository.findByCustomer(user.getCustomerId()));
        return "shop/checkout";
    }

    @PostMapping
    public String placeOrder(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                             @RequestParam int addressId,
                             @RequestParam(defaultValue = "") String couponCode,
                             @RequestParam String paymentMethod,
                             RedirectAttributes redirectAttributes) {
        try {
            int orderId = orderService.placeOrder(user.getCustomerId(), addressId, couponCode, paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " placed");
            return "redirect:/shop/orders/" + orderId;
        } catch (CheckoutException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", DatabaseErrors.message(e));
        }
        redirectAttributes.addFlashAttribute("selectedAddressId", addressId);
        redirectAttributes.addFlashAttribute("couponCode", couponCode);
        redirectAttributes.addFlashAttribute("paymentMethod", paymentMethod);
        return REDIRECT_TO_CHECKOUT;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String invalidValue(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Please choose an address and a payment method");
        return REDIRECT_TO_CHECKOUT;
    }
}
