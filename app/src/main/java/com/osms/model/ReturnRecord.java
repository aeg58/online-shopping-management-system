package com.osms.model;

import java.math.BigDecimal;

public record ReturnRecord(int returnId, int productId, int quantity, String status, BigDecimal refundAmount) {
}
