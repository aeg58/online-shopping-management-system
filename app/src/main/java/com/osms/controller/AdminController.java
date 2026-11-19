package com.osms.controller;

import com.osms.model.SessionUser;
import com.osms.repository.AdminRepository;
import com.osms.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminRepository adminRepository;
    private final AdminService adminService;

    public AdminController(AdminRepository adminRepository, AdminService adminService) {
        this.adminRepository = adminRepository;
        this.adminService = adminService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("roleCounts", adminRepository.countUsersByRole());
        model.addAttribute("orderTotals", adminRepository.findOrderTotals());
        model.addAttribute("latestOrders", adminRepository.findLatestOrders());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminRepository.findUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/active")
    public String setUserActive(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser admin,
                                @PathVariable int id,
                                @RequestParam boolean active,
                                RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, active ? "User activated" : "User deactivated",
                () -> adminService.setUserActive(admin.getUserId(), id, active));
        return "redirect:/admin/users";
    }

    @GetMapping("/sellers")
    public String sellers(Model model) {
        model.addAttribute("sellers", adminRepository.findSellers());
        return "admin/sellers";
    }

    @PostMapping("/sellers/{id}/approve")
    public String approveSeller(@PathVariable int id, RedirectAttributes redirectAttributes) {
        AdminActions.perform(redirectAttributes, "Seller approved", () -> adminService.approveSeller(id));
        return "redirect:/admin/sellers";
    }
}
