package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SellerOrderLine(int orderId, LocalDate orderDate, String productName, int quantity,
                              BigDecimal unitPrice, String orderStatus, String customerFirstName) {
}
