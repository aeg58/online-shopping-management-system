package com.osms.model;

import java.math.BigDecimal;

/** The amounts shown in the order summary. couponError is null when the coupon is valid or empty. */
public record CheckoutSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal shippingFee,
                              BigDecimal total, String couponError) {
}
