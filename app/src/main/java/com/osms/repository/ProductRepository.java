package com.osms.repository;

import com.osms.model.ProductView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private static final String SELECT_PRODUCT = """
            SELECT p.ProductID, p.ProductName, p.Description, p.Price,
                   c.CategoryName, s.StoreName, i.QuantityInStock
            FROM Products p
            JOIN Categories c ON c.CategoryID = p.CategoryID
            JOIN Sellers s ON s.SellerID = p.SellerID
            JOIN Inventory i ON i.ProductID = p.ProductID
            """;

    private static final String FIND_ACTIVE = SELECT_PRODUCT + """
            WHERE p.IsActive = 1
              AND (? = 0 OR p.CategoryID = ?)
              AND p.ProductName LIKE ?
            ORDER BY p.ProductName
            """;

    private static final String FIND_ACTIVE_BY_ID = SELECT_PRODUCT + """
            WHERE p.IsActive = 1 AND p.ProductID = ?
            """;

    private static final RowMapper<ProductView> PRODUCT_MAPPER = (rs, rowNum) -> new ProductView(
            rs.getInt("ProductID"),
            rs.getString("ProductName"),
            rs.getString("Description"),
            rs.getBigDecimal("Price"),
            rs.getString("CategoryName"),
            rs.getString("StoreName"),
            rs.getInt("QuantityInStock")
    );

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductView> findActive(int categoryId, String search) {
        return jdbcTemplate.query(FIND_ACTIVE, PRODUCT_MAPPER, categoryId, categoryId, "%" + search + "%");
    }

    public Optional<ProductView> findActiveById(int productId) {
        return jdbcTemplate.query(FIND_ACTIVE_BY_ID, PRODUCT_MAPPER, productId).stream().findFirst();
    }
}
