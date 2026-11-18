package com.osms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SellerController {

    @GetMapping("/seller")
    public String dashboard() {
        return "seller/dashboard";
    }
}
