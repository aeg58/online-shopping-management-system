package com.osms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentView(String method, BigDecimal amount, String status, LocalDate paidAt) {
}
