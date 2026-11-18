package com.osms.controller;

import com.osms.model.SessionUser;
import com.osms.service.AuthService;
import com.osms.service.LoginFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@ModelAttribute("currentUser") SessionUser currentUser) {
        if (currentUser != null) {
            return "redirect:" + currentUser.getHomePath();
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(defaultValue = "") String username,
                        @RequestParam(defaultValue = "") String password,
                        HttpServletRequest request,
                        Model model) {
        try {
            SessionUser user = authService.login(username, password);
            HttpSession session = request.getSession();
            request.changeSessionId();
            session.setAttribute(SessionUser.SESSION_KEY, user);
            return "redirect:" + user.getHomePath();
        } catch (LoginFailedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("username", username);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out");
        return "redirect:/login";
    }
}
