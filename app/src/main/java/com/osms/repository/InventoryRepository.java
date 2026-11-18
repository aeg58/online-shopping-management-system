package com.osms.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* UPDLOCK keeps the stock row locked until the transaction ends,
       so two orders cannot sell the same last unit. */
    public int lockStock(int productId) {
        Integer stock = jdbcTemplate.queryForObject(
                "SELECT QuantityInStock FROM Inventory WITH (UPDLOCK, ROWLOCK) WHERE ProductID = ?",
                Integer.class, productId);
        return stock == null ? 0 : stock;
    }

    public void decreaseStock(int productId, int quantity) {
        jdbcTemplate.update(
                "UPDATE Inventory SET QuantityInStock = QuantityInStock - ?, LastUpdated = GETDATE() WHERE ProductID = ?",
                quantity, productId);
    }
}
