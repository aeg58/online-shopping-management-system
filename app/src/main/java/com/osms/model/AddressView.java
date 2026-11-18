package com.osms.model;

public record AddressView(int addressId, String title, String city, String district, String addressLine,
                          boolean defaultAddress) {
}
