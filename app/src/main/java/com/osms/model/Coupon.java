package com.osms.model;

import java.math.BigDecimal;

public record Coupon(int couponId, String code, String discountType, BigDecimal discountValue,
                     BigDecimal minOrderAmount, int usageLimit, int usedCount, boolean active,
                     boolean inDateRange) {
}
