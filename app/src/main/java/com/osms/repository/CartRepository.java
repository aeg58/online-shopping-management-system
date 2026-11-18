package com.osms.repository;

import com.osms.model.CartItemView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CartRepository {

    private static final String FIND_ACTIVE_CART = """
            SELECT TOP 1 CartID
            FROM Carts
            WHERE CustomerID = ? AND Status = 'Active'
            ORDER BY CartID
            """;

    /* The locks are held until the transaction ends, so two requests of the same
       customer cannot create two active carts at the same time (business rule BR6). */
    private static final String LOCK_ACTIVE_CART = """
            SELECT TOP 1 CartID
            FROM Carts WITH (UPDLOCK, HOLDLOCK)
            WHERE CustomerID = ? AND Status = 'Active'
            ORDER BY CartID
            """;

    private static final String SELECT_ITEM = """
            SELECT ci.CartItemID, ci.ProductID, p.ProductName, p.Price, ci.Quantity,
                   ci.Quantity * p.Price AS LineTotal, i.QuantityInStock, p.IsActive
            FROM CartItems ci
            JOIN Products p ON p.ProductID = ci.ProductID
            JOIN Inventory i ON i.ProductID = ci.ProductID
            """;

    private static final RowMapper<CartItemView> ITEM_MAPPER = (rs, rowNum) -> new CartItemView(
            rs.getInt("CartItemID"),
            rs.getInt("ProductID"),
            rs.getString("ProductName"),
            rs.getBigDecimal("Price"),
            rs.getInt("Quantity"),
            rs.getBigDecimal("LineTotal"),
            rs.getInt("QuantityInStock"),
            rs.getBoolean("IsActive")
    );

    private final JdbcTemplate jdbcTemplate;

    public CartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Integer> findActiveCartId(int customerId) {
        return jdbcTemplate.queryForList(FIND_ACTIVE_CART, Integer.class, customerId).stream().findFirst();
    }

    public Optional<Integer> lockActiveCartId(int customerId) {
        return jdbcTemplate.queryForList(LOCK_ACTIVE_CART, Integer.class, customerId).stream().findFirst();
    }

    public int createCart(int customerId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Carts (CustomerID, Status) VALUES (?, 'Active')",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, customerId);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public List<CartItemView> findItems(int cartId) {
        return jdbcTemplate.query(SELECT_ITEM + "WHERE ci.CartID = ? ORDER BY ci.CartItemID", ITEM_MAPPER, cartId);
    }

    public Optional<CartItemView> findItem(int cartId, int cartItemId) {
        return jdbcTemplate.query(SELECT_ITEM + "WHERE ci.CartID = ? AND ci.CartItemID = ?",
                ITEM_MAPPER, cartId, cartItemId).stream().findFirst();
    }

    public Optional<Integer> findQuantity(int cartId, int productId) {
        return jdbcTemplate.queryForList("SELECT Quantity FROM CartItems WHERE CartID = ? AND ProductID = ?",
                Integer.class, cartId, productId).stream().findFirst();
    }

    public void insertItem(int cartId, int productId, int quantity) {
        jdbcTemplate.update("INSERT INTO CartItems (CartID, ProductID, Quantity) VALUES (?, ?, ?)",
                cartId, productId, quantity);
    }

    public void updateProductQuantity(int cartId, int productId, int quantity) {
        jdbcTemplate.update("UPDATE CartItems SET Quantity = ? WHERE CartID = ? AND ProductID = ?",
                quantity, cartId, productId);
    }

    public void updateItemQuantity(int cartId, int cartItemId, int quantity) {
        jdbcTemplate.update("UPDATE CartItems SET Quantity = ? WHERE CartID = ? AND CartItemID = ?",
                quantity, cartId, cartItemId);
    }

    public int deleteItem(int cartId, int cartItemId) {
        return jdbcTemplate.update("DELETE FROM CartItems WHERE CartID = ? AND CartItemID = ?", cartId, cartItemId);
    }
}
