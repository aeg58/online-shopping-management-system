package com.osms.repository;

import com.osms.model.RatingSummary;
import com.osms.model.ReviewView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewRepository {

    private static final String FIND_BY_PRODUCT = """
            SELECT c.FirstName, r.Rating, r.Comment, r.CreatedAt
            FROM Reviews r
            JOIN Customers c ON c.CustomerID = r.CustomerID
            WHERE r.ProductID = ?
            ORDER BY r.CreatedAt DESC, r.ReviewID DESC
            """;

    private static final String RATING_SUMMARY = """
            SELECT AVG(CAST(Rating AS DECIMAL(3, 1))) AS AverageRating, COUNT(*) AS ReviewCount
            FROM Reviews
            WHERE ProductID = ?
            """;

    private static final String HAS_PURCHASED = """
            SELECT COUNT(*)
            FROM Orders o
            JOIN OrderItems oi ON oi.OrderID = o.OrderID
            WHERE o.CustomerID = ? AND oi.ProductID = ? AND o.Status = 'Delivered'
            """;

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReviewView> findByProduct(int productId) {
        return jdbcTemplate.query(FIND_BY_PRODUCT, (rs, rowNum) -> new ReviewView(
                rs.getString("FirstName"),
                rs.getInt("Rating"),
                rs.getString("Comment"),
                rs.getDate("CreatedAt").toLocalDate()
        ), productId);
    }

    public boolean hasPurchased(int customerId, int productId) {
        Integer count = jdbcTemplate.queryForObject(HAS_PURCHASED, Integer.class, customerId, productId);
        return count != null && count > 0;
    }

    public boolean hasReviewed(int customerId, int productId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Reviews WHERE CustomerID = ? AND ProductID = ?",
                Integer.class, customerId, productId);
        return count != null && count > 0;
    }

    public void insert(int productId, int customerId, int rating, String comment) {
        jdbcTemplate.update("INSERT INTO Reviews (ProductID, CustomerID, Rating, Comment) VALUES (?, ?, ?, ?)",
                productId, customerId, rating, comment);
    }

    public RatingSummary findRatingSummary(int productId) {
        return jdbcTemplate.queryForObject(RATING_SUMMARY, (rs, rowNum) -> new RatingSummary(
                rs.getBigDecimal("AverageRating"),
                rs.getInt("ReviewCount")
        ), productId);
    }
}
