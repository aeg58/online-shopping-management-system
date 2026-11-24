package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderView(int orderId, LocalDate orderDate, String status, BigDecimal discountAmount,
                        BigDecimal totalAmount, String addressTitle, String city, String district,
                        String addressLine, String couponCode, Integer shippingCompanyId,
                        String shippingCompany, BigDecimal shippingFee) {

    public BigDecimal subtotal() {
        return totalAmount.add(discountAmount).subtract(shippingFee);
    }
}
