package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderSummary(int orderId, LocalDate orderDate, String status, BigDecimal totalAmount) {
}
