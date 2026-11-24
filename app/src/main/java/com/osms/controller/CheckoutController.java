package com.osms.controller;

import com.osms.model.CartItemView;
import com.osms.model.CheckoutSummary;
import com.osms.model.Money;
import com.osms.model.SessionUser;
import com.osms.model.ShippingCompany;
import com.osms.repository.AddressRepository;
import com.osms.repository.ShipmentRepository;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop/checkout")
public class CheckoutController {

    private static final String REDIRECT_TO_CHECKOUT = "redirect:/shop/checkout";

    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final OrderService orderService;
    private final ShipmentRepository shipmentRepository;

    public CheckoutController(CartService cartService, AddressRepository addressRepository,
                              OrderService orderService, ShipmentRepository shipmentRepository) {
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.orderService = orderService;
        this.shipmentRepository = shipmentRepository;
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
        List<ShippingCompany> companies = shipmentRepository.findCompanies();
        Integer selected = (Integer) model.getAttribute("selectedCompanyId");
        int companyId = selected != null ? selected : companies.isEmpty() ? 0 : companies.get(0).shippingCompanyId();
        String couponCode = (String) model.getAttribute("couponCode");
        model.addAttribute("items", items);
        model.addAttribute("companies", companies);
        model.addAttribute("selectedCompanyId", companyId);
        model.addAttribute("summary", orderService.previewOrder(items, companyId, couponCode == null ? "" : couponCode));
        model.addAttribute("addresses", addressRepository.findByCustomer(user.getCustomerId()));
        return "shop/checkout";
    }

    @GetMapping("/summary")
    @ResponseBody
    public Map<String, String> summary(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                       @RequestParam(defaultValue = "0") int shippingCompanyId,
                                       @RequestParam(defaultValue = "") String couponCode) {
        CheckoutSummary summary = orderService.previewOrder(cartService.getItems(user.getCustomerId()),
                shippingCompanyId, couponCode);
        Map<String, String> body = new HashMap<>();
        body.put("subtotal", Money.format(summary.subtotal()));
        body.put("discount", "-" + Money.format(summary.discount()));
        body.put("shipping", Money.format(summary.shippingFee()));
        body.put("total", Money.format(summary.total()));
        body.put("couponError", summary.couponError() == null ? "" : summary.couponError());
        return body;
    }

    @PostMapping
    public String placeOrder(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                             @RequestParam int addressId,
                             @RequestParam int shippingCompanyId,
                             @RequestParam(defaultValue = "") String couponCode,
                             @RequestParam String paymentMethod,
                             RedirectAttributes redirectAttributes) {
        try {
            int orderId = orderService.placeOrder(user.getCustomerId(), addressId, shippingCompanyId, couponCode,
                    paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " placed");
            return "redirect:/shop/orders/" + orderId;
        } catch (CheckoutException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", DatabaseErrors.message(e));
        }
        redirectAttributes.addFlashAttribute("selectedAddressId", addressId);
        redirectAttributes.addFlashAttribute("selectedCompanyId", shippingCompanyId);
        redirectAttributes.addFlashAttribute("couponCode", couponCode);
        redirectAttributes.addFlashAttribute("paymentMethod", paymentMethod);
        return REDIRECT_TO_CHECKOUT;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String invalidValue(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Please choose an address, a shipping company and a payment method");
        return REDIRECT_TO_CHECKOUT;
    }
}
