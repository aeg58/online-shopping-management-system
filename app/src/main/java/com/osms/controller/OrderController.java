package com.osms.controller;

import com.osms.model.OrderView;
import com.osms.model.SessionUser;
import com.osms.repository.OrderRepository;
import com.osms.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/shop/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public OrderController(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/{id}")
    public String order(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                        @PathVariable int id,
                        Model model) {
        OrderView order = orderRepository.findForCustomer(id, user.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("order", order);
        model.addAttribute("items", orderRepository.findItems(id));
        model.addAttribute("payments", paymentRepository.findByOrder(id));
        return "shop/order";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidValue() {
        return "redirect:/shop";
    }
}
