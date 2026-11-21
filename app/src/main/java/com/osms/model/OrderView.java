package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * shippingFee is the fee saved when the order was placed (0 for older orders).
 * shippingCompanyId and shippingCompany are null for orders placed before the company was chosen at checkout.
 */
public record OrderView(int orderId, LocalDate orderDate, String status, BigDecimal discountAmount,
                        BigDecimal totalAmount, String addressTitle, String city, String district,
                        String addressLine, String couponCode, Integer shippingCompanyId,
                        String shippingCompany, BigDecimal shippingFee) {

    /** Products total before the discount and the shipping fee. */
    public BigDecimal subtotal() {
        return totalAmount.add(discountAmount).subtract(shippingFee);
    }
}
