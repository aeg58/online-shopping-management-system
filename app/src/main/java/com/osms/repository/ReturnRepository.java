package com.osms.repository;

import com.osms.model.ReturnView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReturnRepository {

    private static final String FIND_BY_ORDER = """
            SELECT p.ProductName, r.Quantity, r.Reason, r.Status, r.RequestDate, r.RefundAmount
            FROM Returns r
            JOIN Products p ON p.ProductID = r.ProductID
            WHERE r.OrderID = ?
            ORDER BY r.ReturnID DESC
            """;

    private static final String SUM_ACTIVE_RETURNS = """
            SELECT ISNULL(SUM(Quantity), 0)
            FROM Returns
            WHERE OrderID = ? AND ProductID = ? AND Status <> 'Rejected'
            """;

    private final JdbcTemplate jdbcTemplate;

    public ReturnRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* UPDLOCK makes two return requests for the same order item wait for each other,
       so together they can never be more than the ordered quantity. */
    public Optional<Integer> lockOrderedQuantity(int orderId, int productId) {
        return jdbcTemplate.queryForList(
                "SELECT Quantity FROM OrderItems WITH (UPDLOCK, ROWLOCK) WHERE OrderID = ? AND ProductID = ?",
                Integer.class, orderId, productId).stream().findFirst();
    }

    public int sumActiveReturns(int orderId, int productId) {
        Integer sum = jdbcTemplate.queryForObject(SUM_ACTIVE_RETURNS, Integer.class, orderId, productId);
        return sum == null ? 0 : sum;
    }

    public void insert(int orderId, int productId, int quantity, String reason) {
        jdbcTemplate.update("INSERT INTO Returns (OrderID, ProductID, Quantity, Reason) VALUES (?, ?, ?, ?)",
                orderId, productId, quantity, reason);
    }

    public List<ReturnView> findByOrder(int orderId) {
        return jdbcTemplate.query(FIND_BY_ORDER, (rs, rowNum) -> new ReturnView(
                rs.getString("ProductName"),
                rs.getInt("Quantity"),
                rs.getString("Reason"),
                rs.getString("Status"),
                rs.getDate("RequestDate").toLocalDate(),
                rs.getBigDecimal("RefundAmount")
        ), orderId);
    }
}
