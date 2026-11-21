package com.osms.controller;

import com.osms.model.SessionUser;
import com.osms.model.SignupForm;
import com.osms.service.DatabaseErrors;
import com.osms.service.SignupException;
import com.osms.service.SignupService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Sign up for new customers and sellers. A signed-in user is sent to their own home page. */
@Controller
@RequestMapping("/signup")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping
    public String choose(@ModelAttribute("currentUser") SessionUser currentUser) {
        return currentUser != null ? "redirect:" + currentUser.getHomePath() : "signup";
    }

    @GetMapping("/customer")
    public String customerForm(@ModelAttribute("currentUser") SessionUser currentUser, Model model) {
        return showForm(currentUser, model, "signup-customer");
    }

    @GetMapping("/seller")
    public String sellerForm(@ModelAttribute("currentUser") SessionUser currentUser, Model model) {
        return showForm(currentUser, model, "signup-seller");
    }

    @PostMapping("/customer")
    public String signUpCustomer(@ModelAttribute SignupForm form, RedirectAttributes redirectAttributes) {
        try {
            signupService.signUpCustomer(form);
        } catch (SignupException e) {
            return backToForm("/signup/customer", e.getMessage(), form, redirectAttributes);
        } catch (DataIntegrityViolationException e) {
            return backToForm("/signup/customer", DatabaseErrors.readableMessage(e), form, redirectAttributes);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Your account was created. Please sign in.");
        redirectAttributes.addFlashAttribute("username", form.getUsername().trim());
        return "redirect:/login";
    }

    @PostMapping("/seller")
    public String signUpSeller(@ModelAttribute SignupForm form, RedirectAttributes redirectAttributes) {
        try {
            signupService.signUpSeller(form);
        } catch (SignupException e) {
            return backToForm("/signup/seller", e.getMessage(), form, redirectAttributes);
        } catch (DataIntegrityViolationException e) {
            return backToForm("/signup/seller", DatabaseErrors.readableMessage(e), form, redirectAttributes);
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Your store was created. You can sign in after the admin approves it.");
        redirectAttributes.addFlashAttribute("username", form.getUsername().trim());
        return "redirect:/login";
    }

    private String showForm(SessionUser currentUser, Model model, String view) {
        if (currentUser != null) {
            return "redirect:" + currentUser.getHomePath();
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SignupForm());
        }
        model.addAttribute("today", LocalDate.now());
        return view;
    }

    private String backToForm(String path, String message, SignupForm form, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        redirectAttributes.addFlashAttribute("form", form.withoutPasswords());
        return "redirect:" + path;
    }
}
