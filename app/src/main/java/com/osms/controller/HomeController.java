package com.osms.controller;

import com.osms.model.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@ModelAttribute("currentUser") SessionUser currentUser) {
        return "redirect:" + (currentUser != null ? currentUser.getHomePath() : "/shop");
    }
}
