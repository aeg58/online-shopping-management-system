package com.osms.repository;

import com.osms.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
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

    public Optional<Integer> findRoleId(String roleName) {
        return jdbcTemplate.queryForList("SELECT RoleID FROM Roles WHERE RoleName = ?", Integer.class, roleName)
                .stream().findFirst();
    }

    public int insertUser(int roleId, String username, String email, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Users (RoleID, Username, Email, PasswordHash) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, roleId);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, passwordHash);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void insertCustomer(int userId, String firstName, String lastName, String phone, LocalDate birthDate) {
        jdbcTemplate.update("""
                INSERT INTO Customers (UserID, FirstName, LastName, Phone, BirthDate)
                VALUES (?, ?, ?, ?, ?)
                """, userId, firstName, lastName, phone, birthDate == null ? null : Date.valueOf(birthDate));
    }

    public void insertSeller(int userId, String storeName, String taxNumber, String phone) {
        jdbcTemplate.update("INSERT INTO Sellers (UserID, StoreName, TaxNumber, Phone) VALUES (?, ?, ?, ?)",
                userId, storeName, taxNumber, phone);
    }
}
