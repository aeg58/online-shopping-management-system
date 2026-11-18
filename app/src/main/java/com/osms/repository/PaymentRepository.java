package com.osms.repository;

import com.osms.model.PaymentView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertCompleted(int orderId, String method, BigDecimal amount) {
        jdbcTemplate.update("""
                INSERT INTO Payments (OrderID, Method, Amount, Status, PaidAt)
                VALUES (?, ?, ?, 'Completed', CAST(GETDATE() AS DATE))
                """, orderId, method, amount);
    }

    public void insertPending(int orderId, String method, BigDecimal amount) {
        jdbcTemplate.update("""
                INSERT INTO Payments (OrderID, Method, Amount, Status, PaidAt)
                VALUES (?, ?, ?, 'Pending', NULL)
                """, orderId, method, amount);
    }

    public List<PaymentView> findByOrder(int orderId) {
        return jdbcTemplate.query(
                "SELECT Method, Amount, Status, PaidAt FROM Payments WHERE OrderID = ? ORDER BY PaymentID",
                (rs, rowNum) -> {
                    Date paidAt = rs.getDate("PaidAt");
                    return new PaymentView(
                            rs.getString("Method"),
                            rs.getBigDecimal("Amount"),
                            rs.getString("Status"),
                            paidAt == null ? null : paidAt.toLocalDate());
                }, orderId);
    }
}
