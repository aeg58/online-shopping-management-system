package com.osms.controller;

import com.osms.model.OrderItemView;
import com.osms.model.OrderView;
import com.osms.model.SessionUser;
import com.osms.repository.OrderRepository;
import com.osms.repository.PaymentRepository;
import com.osms.repository.ReturnRepository;
import com.osms.repository.ShipmentRepository;
import com.osms.service.CustomerRequestException;
import com.osms.service.DatabaseErrors;
import com.osms.service.ReturnService;
import com.osms.service.ReviewService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/shop/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final ReturnRepository returnRepository;
    private final ReturnService returnService;
    private final ReviewService reviewService;

    public OrderController(OrderRepository orderRepository, PaymentRepository paymentRepository,
                           ShipmentRepository shipmentRepository, ReturnRepository returnRepository,
                           ReturnService returnService, ReviewService reviewService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.returnRepository = returnRepository;
        this.returnService = returnService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String orders(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        model.addAttribute("orders", orderRepository.findByCustomer(user.getCustomerId()));
        return "shop/orders";
    }

    @GetMapping("/{id}")
    public String order(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                        @PathVariable int id,
                        Model model) {
        OrderView order = orderRepository.findForCustomer(id, user.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<OrderItemView> items = orderRepository.findItems(id);
        List<OrderItemView> returnableItems = "Delivered".equals(order.status())
                ? items.stream().filter(item -> item.remainingQuantity() > 0).toList()
                : List.of();

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("returnableItems", returnableItems);
        model.addAttribute("payments", paymentRepository.findByOrder(id));
        model.addAttribute("shipment", shipmentRepository.findByOrder(id).orElse(null));
        model.addAttribute("returns", returnRepository.findByOrder(id));
        return "shop/order";
    }

    @PostMapping("/{id}/return")
    public String requestReturn(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                @PathVariable int id,
                                @RequestParam int productId,
                                @RequestParam int quantity,
                                @RequestParam(defaultValue = "") String reason,
                                RedirectAttributes redirectAttributes) {
        try {
            returnService.requestReturn(user.getCustomerId(), id, productId, quantity, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Your return request was sent");
        } catch (CustomerRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", DatabaseErrors.message(e));
        }
        return "redirect:/shop/orders/" + id;
    }

    @PostMapping("/review")
    public String addReview(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                            @RequestParam int productId,
                            @RequestParam int rating,
                            @RequestParam(defaultValue = "") String comment,
                            RedirectAttributes redirectAttributes) {
        try {
            reviewService.addReview(user.getCustomerId(), productId, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your review");
        } catch (CustomerRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/shop/product/" + productId;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String invalidValue(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Please enter valid values");
        return "redirect:/shop/orders";
    }
}
