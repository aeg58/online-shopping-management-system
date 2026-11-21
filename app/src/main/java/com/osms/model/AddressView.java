package com.osms.model;

/** usedInOrders is true when an order ships to this address; such an address cannot be deleted. */
public record AddressView(int addressId, String title, String country, String city, String district,
                          String addressLine, String postalCode, boolean defaultAddress, boolean usedInOrders) {
}
