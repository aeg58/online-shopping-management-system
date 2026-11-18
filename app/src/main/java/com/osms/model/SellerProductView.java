package com.osms.model;

import java.math.BigDecimal;

public record SellerProductView(int productId, String productName, String description, BigDecimal price,
                                boolean active, String categoryName, int quantityInStock) {
}
