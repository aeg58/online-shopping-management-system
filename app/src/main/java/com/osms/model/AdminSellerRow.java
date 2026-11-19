package com.osms.model;

public record AdminSellerRow(int sellerId, String storeName, String taxNumber, String username, boolean approved) {
}
