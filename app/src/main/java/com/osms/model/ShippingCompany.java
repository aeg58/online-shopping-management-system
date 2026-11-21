package com.osms.model;

import java.math.BigDecimal;

public record ShippingCompany(int shippingCompanyId, String companyName, BigDecimal baseFee) {
}
