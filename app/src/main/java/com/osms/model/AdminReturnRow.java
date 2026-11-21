package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminReturnRow(int returnId, int orderId, String productName, int quantity, String reason,
                             String status, LocalDate requestDate, BigDecimal refundAmount, String rejectReason) {
}
