package com.osms.model;

import java.math.BigDecimal;

public record CheckoutSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal shippingFee,
                              BigDecimal total, String couponError) {
}
