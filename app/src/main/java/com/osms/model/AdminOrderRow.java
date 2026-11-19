package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminOrderRow(int orderId, LocalDate orderDate, String customerName, String status,
                            BigDecimal totalAmount) {
}
