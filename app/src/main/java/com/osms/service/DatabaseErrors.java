package com.osms.service;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabaseErrors {

    private static final Pattern CONSTRAINT_NAME = Pattern.compile("constraint ['\"](\\w+)['\"]");

    private DatabaseErrors() {
    }

    /** Builds a readable message that names the database constraint which rejected the value. */
    public static String message(DataIntegrityViolationException exception) {
        Matcher matcher = CONSTRAINT_NAME.matcher(exception.getMostSpecificCause().getMessage());
        String constraint = matcher.find() ? matcher.group(1) : "unknown";
        return "The value breaks a database rule: " + constraint;
    }
}
