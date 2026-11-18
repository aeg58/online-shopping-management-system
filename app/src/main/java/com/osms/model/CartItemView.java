package com.osms.model;

import java.math.BigDecimal;

public record CartItemView(int cartItemId, int productId, String productName, BigDecimal price, int quantity,
                           BigDecimal lineTotal, int quantityInStock, boolean productActive) {
}
