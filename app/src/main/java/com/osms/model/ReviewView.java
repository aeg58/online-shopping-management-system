package com.osms.model;

import java.time.LocalDate;

public record ReviewView(String firstName, int rating, String comment, LocalDate createdAt) {
}
