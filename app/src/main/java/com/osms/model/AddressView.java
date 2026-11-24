package com.osms.model;

public record AddressView(int addressId, String title, String country, String city, String district,
                          String addressLine, String postalCode, boolean defaultAddress, boolean usedInOrders) {
}
