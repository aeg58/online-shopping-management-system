package com.osms.repository;

import com.osms.model.SellerOrderLine;
import com.osms.model.SellerProductView;
import com.osms.model.SellerSales;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class SellerRepository {

    private static final String SELECT_PRODUCT = """
            SELECT p.ProductID, p.ProductName, p.Description, p.Price, p.IsActive,
                   c.CategoryName, i.QuantityInStock
            FROM Products p
            JOIN Categories c ON c.CategoryID = p.CategoryID
            JOIN Inventory i ON i.ProductID = p.ProductID
            """;

    private static final String SALES_SUMMARY = """
            SELECT ISNULL(SUM(oi.Quantity), 0) AS UnitsSold,
                   ISNULL(SUM(oi.Quantity * oi.UnitPrice), 0) AS Revenue
            FROM OrderItems oi
            JOIN Products p ON p.ProductID = oi.ProductID
            JOIN Orders o ON o.OrderID = oi.OrderID
            WHERE p.SellerID = ? AND o.Status <> 'Cancelled'
            """;

    private static final String INSERT_PRODUCT = """
            INSERT INTO Products (SellerID, CategoryID, ProductName, Description, Price)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_PRODUCT = """
            UPDATE Products
            SET Price = ?, Description = ?, IsActive = ?
            WHERE ProductID = ? AND SellerID = ?
            """;

    private static final String UPDATE_STOCK = """
            UPDATE i
            SET i.QuantityInStock = ?, i.LastUpdated = GETDATE()
            FROM Inventory i
            JOIN Products p ON p.ProductID = i.ProductID
            WHERE i.ProductID = ? AND p.SellerID = ?
            """;

    private static final String FIND_ORDER_LINES = """
            SELECT o.OrderID, o.OrderDate, p.ProductName, oi.Quantity, oi.UnitPrice, o.Status, c.FirstName
            FROM OrderItems oi
            JOIN Products p ON p.ProductID = oi.ProductID
            JOIN Orders o ON o.OrderID = oi.OrderID
            JOIN Customers c ON c.CustomerID = o.CustomerID
            WHERE p.SellerID = ?
            ORDER BY o.OrderDate DESC, o.OrderID DESC, oi.OrderItemID
            """;

    private static final RowMapper<SellerProductView> PRODUCT_MAPPER = (rs, rowNum) -> new SellerProductView(
            rs.getInt("ProductID"),
            rs.getString("ProductName"),
            rs.getString("Description"),
            rs.getBigDecimal("Price"),
            rs.getBoolean("IsActive"),
            rs.getString("CategoryName"),
            rs.getInt("QuantityInStock")
    );

    private final JdbcTemplate jdbcTemplate;

    public SellerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countProducts(int sellerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Products WHERE SellerID = ?", Integer.class, sellerId);
        return count == null ? 0 : count;
    }

    public SellerSales findSales(int sellerId) {
        return jdbcTemplate.queryForObject(SALES_SUMMARY, (rs, rowNum) -> new SellerSales(
                rs.getInt("UnitsSold"),
                rs.getBigDecimal("Revenue")
        ), sellerId);
    }

    public List<SellerProductView> findLowStock(int sellerId) {
        return jdbcTemplate.query(
                SELECT_PRODUCT + "WHERE p.SellerID = ? AND i.QuantityInStock < 5 ORDER BY i.QuantityInStock, p.ProductName",
                PRODUCT_MAPPER, sellerId);
    }

    public List<SellerProductView> findProducts(int sellerId) {
        return jdbcTemplate.query(SELECT_PRODUCT + "WHERE p.SellerID = ? ORDER BY p.ProductName",
                PRODUCT_MAPPER, sellerId);
    }

    public Optional<SellerProductView> findProduct(int sellerId, int productId) {
        return jdbcTemplate.query(SELECT_PRODUCT + "WHERE p.SellerID = ? AND p.ProductID = ?",
                PRODUCT_MAPPER, sellerId, productId).stream().findFirst();
    }

    public int insertProduct(int sellerId, int categoryId, String name, String description, BigDecimal price) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_PRODUCT, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, sellerId);
            statement.setInt(2, categoryId);
            statement.setString(3, name);
            statement.setString(4, description);
            statement.setBigDecimal(5, price);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void insertInventory(int productId, int stock) {
        jdbcTemplate.update("INSERT INTO Inventory (ProductID, QuantityInStock) VALUES (?, ?)", productId, stock);
    }

    public int updateProduct(int sellerId, int productId, BigDecimal price, String description, boolean active) {
        return jdbcTemplate.update(UPDATE_PRODUCT, price, description, active, productId, sellerId);
    }

    public int updateStock(int sellerId, int productId, int stock) {
        return jdbcTemplate.update(UPDATE_STOCK, stock, productId, sellerId);
    }

    public List<SellerOrderLine> findOrderLines(int sellerId) {
        return jdbcTemplate.query(FIND_ORDER_LINES, (rs, rowNum) -> new SellerOrderLine(
                rs.getInt("OrderID"),
                rs.getDate("OrderDate").toLocalDate(),
                rs.getString("ProductName"),
                rs.getInt("Quantity"),
                rs.getBigDecimal("UnitPrice"),
                rs.getString("Status"),
                rs.getString("FirstName")
        ), sellerId);
    }
}
