package com.osms.model;

import java.math.BigDecimal;

public record ProductView(int productId, String productName, String description, BigDecimal price,
                          String categoryName, String storeName, int quantityInStock) {
}
