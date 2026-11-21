package com.osms.model;

import java.math.BigDecimal;

/** returnStatus is the status of the latest return request for this item, or null when there is none. */
public record OrderItemView(int productId, String productName, int quantity, BigDecimal unitPrice,
                            BigDecimal lineTotal, int returnedQuantity, String returnStatus,
                            boolean returnRejected) {

    public int remainingQuantity() {
        return quantity - returnedQuantity;
    }

    /** A rejected return closes the product: the customer cannot ask again for it in this order. */
    public boolean canRequestReturn() {
        return !returnRejected && remainingQuantity() > 0;
    }
}
