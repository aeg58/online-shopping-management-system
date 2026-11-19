package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CouponRow(int couponId, String code, String discountType, BigDecimal discountValue,
                        BigDecimal minOrderAmount, LocalDate startDate, LocalDate endDate, int usageLimit,
                        int usedCount, boolean active) {
}
