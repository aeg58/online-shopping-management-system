package com.osms.model;

import java.time.LocalDate;

public record ShipmentView(String companyName, String trackingNumber, String status, LocalDate shippedDate,
                           LocalDate deliveredDate) {
}
