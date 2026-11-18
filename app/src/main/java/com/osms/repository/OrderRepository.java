package com.osms.repository;

import com.osms.model.OrderItemView;
import com.osms.model.OrderView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private static final String INSERT_ORDER = """
            INSERT INTO Orders (CustomerID, AddressID, CouponID, Status, DiscountAmount, TotalAmount)
            VALUES (?, ?, ?, 'Pending', ?, ?)
            """;

    private static final String FIND_FOR_CUSTOMER = """
            SELECT o.OrderID, o.OrderDate, o.Status, o.DiscountAmount, o.TotalAmount,
                   a.Title, a.City, a.District, a.AddressLine, c.Code
            FROM Orders o
            JOIN Addresses a ON a.AddressID = o.AddressID
            LEFT JOIN Coupons c ON c.CouponID = o.CouponID
            WHERE o.OrderID = ? AND o.CustomerID = ?
            """;

    private static final String FIND_ITEMS = """
            SELECT oi.ProductID, p.ProductName, oi.Quantity, oi.UnitPrice,
                   oi.Quantity * oi.UnitPrice AS LineTotal
            FROM OrderItems oi
            JOIN Products p ON p.ProductID = oi.ProductID
            WHERE oi.OrderID = ?
            ORDER BY oi.OrderItemID
            """;

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insertOrder(int customerId, int addressId, Integer couponId, BigDecimal discount, BigDecimal total) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_ORDER, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, customerId);
            statement.setInt(2, addressId);
            statement.setObject(3, couponId, Types.INTEGER);
            statement.setBigDecimal(4, discount);
            statement.setBigDecimal(5, total);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void insertItem(int orderId, int productId, int quantity, BigDecimal unitPrice) {
        jdbcTemplate.update("INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)",
                orderId, productId, quantity, unitPrice);
    }

    public void markPaid(int orderId) {
        jdbcTemplate.update("UPDATE Orders SET Status = 'Paid' WHERE OrderID = ?", orderId);
    }

    public Optional<OrderView> findForCustomer(int orderId, int customerId) {
        return jdbcTemplate.query(FIND_FOR_CUSTOMER, (rs, rowNum) -> new OrderView(
                rs.getInt("OrderID"),
                rs.getDate("OrderDate").toLocalDate(),
                rs.getString("Status"),
                rs.getBigDecimal("DiscountAmount"),
                rs.getBigDecimal("TotalAmount"),
                rs.getString("Title"),
                rs.getString("City"),
                rs.getString("District"),
                rs.getString("AddressLine"),
                rs.getString("Code")
        ), orderId, customerId).stream().findFirst();
    }

    public List<OrderItemView> findItems(int orderId) {
        return jdbcTemplate.query(FIND_ITEMS, (rs, rowNum) -> new OrderItemView(
                rs.getInt("ProductID"),
                rs.getString("ProductName"),
                rs.getInt("Quantity"),
                rs.getBigDecimal("UnitPrice"),
                rs.getBigDecimal("LineTotal")
        ), orderId);
    }
}
