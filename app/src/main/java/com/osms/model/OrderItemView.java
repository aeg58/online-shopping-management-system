package com.osms.model;

import java.math.BigDecimal;

public record OrderItemView(int productId, String productName, int quantity, BigDecimal unitPrice,
                            BigDecimal lineTotal, int returnedQuantity, String returnStatus,
                            boolean returnRejected) {

    public int remainingQuantity() {
        return quantity - returnedQuantity;
    }

    public boolean canRequestReturn() {
        return !returnRejected && remainingQuantity() > 0;
    }
}
