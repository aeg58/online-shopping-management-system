package com.osms.model;

public record AdminUserRow(int userId, String username, String email, String roleName, boolean active) {
}
