package com.osms.model;

import java.io.Serializable;

public class SessionUser implements Serializable {

    public static final String SESSION_KEY = "currentUser";
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_SELLER = "Seller";
    public static final String ROLE_CUSTOMER = "Customer";

    private final int userId;
    private final String username;
    private final String roleName;
    private final Integer customerId;
    private final Integer sellerId;

    public SessionUser(int userId, String username, String roleName, Integer customerId, Integer sellerId) {
        this.userId = userId;
        this.username = username;
        this.roleName = roleName;
        this.customerId = customerId;
        this.sellerId = sellerId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
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

    public String getHomePath() {
        return switch (roleName) {
            case ROLE_ADMIN -> "/admin";
            case ROLE_SELLER -> "/seller";
            default -> "/shop";
        };
    }
}
