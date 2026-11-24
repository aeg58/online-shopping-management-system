package com.osms.controller;

import com.osms.model.SessionUser;
import com.osms.service.ProductImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LayoutAdvice {

    private final ProductImageService productImages;

    public LayoutAdvice(ProductImageService productImages) {
        this.productImages = productImages;
    }

    @ModelAttribute("productImages")
    public ProductImageService productImages() {
        return productImages;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
    }
}
