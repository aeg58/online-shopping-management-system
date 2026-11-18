package com.osms.service;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabaseErrors {

    private static final Pattern CONSTRAINT_NAME = Pattern.compile("constraint ['\"](\\w+)['\"]");

    private DatabaseErrors() {
    }

    /** Returns the name of the database constraint which rejected the value. */
    public static String constraintName(DataIntegrityViolationException exception) {
        Matcher matcher = CONSTRAINT_NAME.matcher(exception.getMostSpecificCause().getMessage());
        return matcher.find() ? matcher.group(1) : "unknown";
    }

    /** Builds a readable message that names the database constraint which rejected the value. */
    public static String message(DataIntegrityViolationException exception) {
        return "The value breaks a database rule: " + constraintName(exception);
    }
}
