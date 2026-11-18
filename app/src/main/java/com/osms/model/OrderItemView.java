package com.osms.model;

import java.math.BigDecimal;

public record OrderItemView(int productId, String productName, int quantity, BigDecimal unitPrice,
                            BigDecimal lineTotal, int returnedQuantity) {

    public int remainingQuantity() {
        return quantity - returnedQuantity;
    }
}
