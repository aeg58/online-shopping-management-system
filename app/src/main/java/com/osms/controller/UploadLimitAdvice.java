package com.osms.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadLimitAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "The photo can be at most 2 MB");
        return "redirect:/seller/products/new";
    }
}
