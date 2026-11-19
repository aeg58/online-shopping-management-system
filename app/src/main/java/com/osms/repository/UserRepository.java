package com.osms.repository;

import com.osms.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final String FIND_BY_USERNAME = """
            SELECT u.UserID, u.Username, u.Email, u.PasswordHash, u.IsActive, r.RoleName,
                   c.CustomerID, s.SellerID, s.IsApproved
            FROM Users u
            JOIN Roles r ON r.RoleID = u.RoleID
            LEFT JOIN Customers c ON c.UserID = u.UserID
            LEFT JOIN Sellers s ON s.UserID = u.UserID
            WHERE u.Username = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findByUsername(String username) {
        List<UserAccount> users = jdbcTemplate.query(FIND_BY_USERNAME, (rs, rowNum) -> new UserAccount(
                rs.getInt("UserID"),
                rs.getString("Username"),
                rs.getString("Email"),
                rs.getString("PasswordHash"),
                rs.getBoolean("IsActive"),
                rs.getString("RoleName"),
                rs.getObject("CustomerID", Integer.class),
                rs.getObject("SellerID", Integer.class),
                rs.getObject("IsApproved", Boolean.class)
        ), username);
        return users.stream().findFirst();
    }

    public boolean isActive(int userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Users WHERE UserID = ? AND IsActive = 1", Integer.class, userId);
        return count != null && count > 0;
    }
}
