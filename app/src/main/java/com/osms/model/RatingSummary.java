package com.osms.model;

import java.math.BigDecimal;

public record RatingSummary(BigDecimal average, int reviewCount) {
}
