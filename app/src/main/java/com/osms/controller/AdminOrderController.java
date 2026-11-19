package com.osms.controller;

import com.osms.model.AdminOrderRow;
import com.osms.model.OrderView;
import com.osms.repository.AdminRepository;
import com.osms.repository.OrderRepository;
import com.osms.repository.PaymentRepository;
import com.osms.repository.ReturnRepository;
import com.osms.repository.ShipmentRepository;
import com.osms.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    private static final List<String> ORDER_STATUSES = List.of("Pending", "Paid", "Shipped", "Delivered", "Cancelled");

    private final AdminRepository adminRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final ReturnRepository returnRepository;
    private final AdminService adminService;

    public AdminOrderController(AdminRepository adminRepository, OrderRepository orderRepository,
                                PaymentRepository paymentRepository, ShipmentRepository shipmentRepository,
                                ReturnRepository returnRepository, AdminService adminService) {
        this.adminRepository = adminRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.returnRepository = returnRepository;
        this.adminService = adminService;
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", adminRepository.findOrders());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String order(@PathVariable int id, Model model) {
        OrderView order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        AdminOrderRow summary = adminRepository.findOrderRow(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("order", order);
        model.addAttribute("customerName", summary.customerName());
        model.addAttribute("items", orderRepository.findItems(id));
        model.addAttribute("payments", paymentRepository.findByOrder(id));
        model.addAttribute("shipment", shipmentRepository.findByOrder(id).orElse(null));
        model.addAttribute("returns", returnRepository.findByOrder(id));
        model.addAttribute("companies", shipmentRepository.findCompanies());
        model.addAttribute("statuses", ORDER_STATUSES);
        return "admin/order";
    }

    @PostMapping("/orders/{id}/status")
    public String changeStatus(@PathVariable int id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, "Order status changed",
                () -> adminService.changeOrderStatus(id, status));
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/shipment")
    public String createShipment(@PathVariable int id,
                                 @RequestParam int shippingCompanyId,
                                 @RequestParam(defaultValue = "") String trackingNumber,
                                 RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, "Shipment created",
                () -> adminService.createShipment(id, shippingCompanyId, trackingNumber));
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/deliver")
    public String markDelivered(@PathVariable int id, RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, "Order marked as delivered",
                () -> adminService.markDelivered(id));
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/returns")
    public String returns(Model model) {
        model.addAttribute("returns", adminRepository.findReturns());
        return "admin/returns";
    }

    @PostMapping("/returns/{id}/approve")
    public String approveReturn(@PathVariable int id, RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, "Return approved", () -> adminService.approveReturn(id));
        return "redirect:/admin/returns";
    }

    @PostMapping("/returns/{id}/reject")
    public String rejectReturn(@PathVariable int id, RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, "Return rejected", () -> adminService.rejectReturn(id));
        return "redirect:/admin/returns";
    }

    @PostMapping("/returns/{id}/refund")
    public String refundReturn(@PathVariable int id, RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, "Return refunded and stock updated",
                () -> adminService.refundReturn(id));
        return "redirect:/admin/returns";
    }
}
