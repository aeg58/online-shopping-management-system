package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReturnView(String productName, int quantity, String reason, String status, LocalDate requestDate,
                         BigDecimal refundAmount) {
}
