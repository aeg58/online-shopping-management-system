package com.osms.model;

public class UserAccount {

    private final int userId;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final String roleName;
    private final Integer customerId;
    private final Integer sellerId;
    private final Boolean sellerApproved;

    public UserAccount(int userId, String username, String email, String passwordHash, boolean active,
                       String roleName, Integer customerId, Integer sellerId, Boolean sellerApproved) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.roleName = roleName;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.sellerApproved = sellerApproved;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public String getRoleName() {
        return roleName;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public Boolean getSellerApproved() {
        return sellerApproved;
    }
}
