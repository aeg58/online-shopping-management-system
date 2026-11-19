package com.osms.service;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabaseErrors {

    private static final Pattern CONSTRAINT_NAME = Pattern.compile("constraint ['\"](\\w+)['\"]");

    private static final Map<String, String> READABLE_MESSAGES = Map.ofEntries(
            Map.entry("UQ_Categories_CategoryName", "A category with this name already exists"),
            Map.entry("UQ_Coupons_Code", "A coupon with this code already exists"),
            Map.entry("CK_Coupons_DiscountType", "Discount type must be Percent or Fixed"),
            Map.entry("CK_Coupons_DiscountValue", "Discount value must be greater than 0"),
            Map.entry("CK_Coupons_Percent", "A percent discount cannot be more than 100"),
            Map.entry("CK_Coupons_MinOrderAmount", "Minimum order amount cannot be negative"),
            Map.entry("CK_Coupons_Dates", "End date cannot be before start date"),
            Map.entry("CK_Coupons_Usage", "Usage limit must be greater than 0"),
            Map.entry("CK_Orders_Status", "This order status is not allowed"),
            Map.entry("UQ_Shipments_OrderID", "This order already has a shipment"),
            Map.entry("UQ_Shipments_TrackingNumber", "This tracking number is already used"),
            Map.entry("FK_Shipments_ShippingCompanies", "Please choose a shipping company")
    );

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

    /** Explains the broken rule in plain words and keeps the constraint name visible. */
    public static String readableMessage(DataIntegrityViolationException exception) {
        String constraint = constraintName(exception);
        String text = READABLE_MESSAGES.get(constraint);
        return text == null ? message(exception) : text + " (" + constraint + ")";
    }
}
