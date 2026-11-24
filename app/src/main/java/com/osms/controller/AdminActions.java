package com.osms.controller;

import com.osms.service.AdminRequestException;
import com.osms.service.DatabaseErrors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

final class AdminActions {

    private AdminActions() {
    }

    static void perform(RedirectAttributes redirectAttributes, String successMessage, Runnable action) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (AdminRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", DatabaseErrors.readableMessage(e));
        }
    }
}
