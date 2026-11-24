package com.osms.repository;

import com.osms.model.AdminOrderRow;
import com.osms.model.AdminReturnRow;
import com.osms.model.AdminSellerRow;
import com.osms.model.AdminUserRow;
import com.osms.model.OrderTotals;
import com.osms.model.ReturnRecord;
import com.osms.model.RoleCount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepository {

    private static final String COUNT_USERS_BY_ROLE = """
            SELECT r.RoleName, COUNT(u.UserID) AS Total
            FROM Roles r
            LEFT JOIN Users u ON u.RoleID = r.RoleID
            GROUP BY r.RoleName
            ORDER BY r.RoleName
            """;

    private static final String ORDER_TOTALS = """
            SELECT COUNT(*) AS TotalOrders,
                   ISNULL(SUM(CASE WHEN Status <> 'Cancelled' THEN TotalAmount ELSE 0 END), 0) AS Revenue
            FROM Orders
            """;

    private static final String ORDER_ROW_COLUMNS = """
            o.OrderID, o.OrderDate, c.FirstName + ' ' + c.LastName AS CustomerName, o.Status, o.TotalAmount
            FROM Orders o
            JOIN Customers c ON c.CustomerID = o.CustomerID
            """;

    private static final String FIND_USERS = """
            SELECT u.UserID, u.Username, u.Email, r.RoleName, u.IsActive
            FROM Users u
            JOIN Roles r ON r.RoleID = u.RoleID
            ORDER BY r.RoleName, u.Username
            """;

    private static final String FIND_SELLERS = """
            SELECT s.SellerID, s.StoreName, s.TaxNumber, u.Username, s.IsApproved
            FROM Sellers s
            JOIN Users u ON u.UserID = s.UserID
            ORDER BY s.IsApproved, s.StoreName
            """;

    private static final String FIND_RETURNS = """
            SELECT r.ReturnID, r.OrderID, p.ProductName, r.Quantity, r.Reason, r.Status, r.RequestDate, r.RefundAmount,
                   r.RejectReason
            FROM Returns r
            JOIN Products p ON p.ProductID = r.ProductID
            ORDER BY r.RequestDate DESC, r.ReturnID DESC
            """;

    private static final String LOCK_RETURN = """
            SELECT r.ReturnID, r.ProductID, r.Quantity, r.Status, r.Quantity * oi.UnitPrice AS RefundAmount
            FROM Returns r WITH (UPDLOCK, ROWLOCK)
            JOIN OrderItems oi ON oi.OrderID = r.OrderID AND oi.ProductID = r.ProductID
            WHERE r.ReturnID = ?
            """;

    private static final RowMapper<AdminOrderRow> ORDER_ROW_MAPPER = (rs, rowNum) -> new AdminOrderRow(
            rs.getInt("OrderID"),
            rs.getDate("OrderDate").toLocalDate(),
            rs.getString("CustomerName"),
            rs.getString("Status"),
            rs.getBigDecimal("TotalAmount")
    );

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoleCount> countUsersByRole() {
        return jdbcTemplate.query(COUNT_USERS_BY_ROLE,
                (rs, rowNum) -> new RoleCount(rs.getString("RoleName"), rs.getInt("Total")));
    }

    public OrderTotals findOrderTotals() {
        return jdbcTemplate.queryForObject(ORDER_TOTALS,
                (rs, rowNum) -> new OrderTotals(rs.getInt("TotalOrders"), rs.getBigDecimal("Revenue")));
    }

    public List<AdminOrderRow> findLatestOrders() {
        return jdbcTemplate.query("SELECT TOP 5 " + ORDER_ROW_COLUMNS + "ORDER BY o.OrderDate DESC, o.OrderID DESC",
                ORDER_ROW_MAPPER);
    }

    public List<AdminOrderRow> findOrders() {
        return jdbcTemplate.query("SELECT " + ORDER_ROW_COLUMNS + "ORDER BY o.OrderDate DESC, o.OrderID DESC",
                ORDER_ROW_MAPPER);
    }

    public Optional<AdminOrderRow> findOrderRow(int orderId) {
        return jdbcTemplate.query("SELECT " + ORDER_ROW_COLUMNS + "WHERE o.OrderID = ?",
                ORDER_ROW_MAPPER, orderId).stream().findFirst();
    }

    public List<AdminUserRow> findUsers() {
        return jdbcTemplate.query(FIND_USERS, (rs, rowNum) -> new AdminUserRow(
                rs.getInt("UserID"),
                rs.getString("Username"),
                rs.getString("Email"),
                rs.getString("RoleName"),
                rs.getBoolean("IsActive")
        ));
    }

    public int setUserActive(int userId, boolean active) {
        return jdbcTemplate.update("UPDATE Users SET IsActive = ? WHERE UserID = ?", active, userId);
    }

    public List<AdminSellerRow> findSellers() {
        return jdbcTemplate.query(FIND_SELLERS, (rs, rowNum) -> new AdminSellerRow(
                rs.getInt("SellerID"),
                rs.getString("StoreName"),
                rs.getString("TaxNumber"),
                rs.getString("Username"),
                rs.getBoolean("IsApproved")
        ));
    }

    public int approveSeller(int sellerId) {
        return jdbcTemplate.update("UPDATE Sellers SET IsApproved = 1 WHERE SellerID = ?", sellerId);
    }

    public Optional<String> lockOrderStatus(int orderId) {
        return jdbcTemplate.queryForList("SELECT Status FROM Orders WITH (UPDLOCK, ROWLOCK) WHERE OrderID = ?",
                String.class, orderId).stream().findFirst();
    }

    public int updateOrderStatus(int orderId, String status) {
        return jdbcTemplate.update("UPDATE Orders SET Status = ? WHERE OrderID = ?", status, orderId);
    }

    public void restoreStock(int orderId) {
        jdbcTemplate.update("""
                UPDATE i
                SET i.QuantityInStock = i.QuantityInStock + oi.Quantity, i.LastUpdated = GETDATE()
                FROM Inventory i
                JOIN OrderItems oi ON oi.ProductID = i.ProductID
                WHERE oi.OrderID = ?
                """, orderId);
    }

    public void releaseCoupon(int orderId) {
        jdbcTemplate.update("""
                UPDATE c
                SET c.UsedCount = c.UsedCount - 1
                FROM Coupons c
                JOIN Orders o ON o.CouponID = c.CouponID
                WHERE o.OrderID = ? AND c.UsedCount > 0
                """, orderId);
    }

    public void cancelPayments(int orderId) {
        jdbcTemplate.update("""
                UPDATE Payments
                SET Status = CASE WHEN Status = 'Completed' THEN 'Refunded' ELSE 'Failed' END
                WHERE OrderID = ? AND Status IN ('Completed', 'Pending')
                """, orderId);
    }

    public void completeCashPayment(int orderId) {
        jdbcTemplate.update("""
                UPDATE Payments
                SET Status = 'Completed', PaidAt = CAST(GETDATE() AS DATE)
                WHERE OrderID = ? AND Method = 'CashOnDelivery' AND Status = 'Pending'
                """, orderId);
    }

    public List<AdminReturnRow> findReturns() {
        return jdbcTemplate.query(FIND_RETURNS, (rs, rowNum) -> new AdminReturnRow(
                rs.getInt("ReturnID"),
                rs.getInt("OrderID"),
                rs.getString("ProductName"),
                rs.getInt("Quantity"),
                rs.getString("Reason"),
                rs.getString("Status"),
                rs.getDate("RequestDate").toLocalDate(),
                rs.getBigDecimal("RefundAmount"),
                rs.getString("RejectReason")
        ));
    }

    public Optional<ReturnRecord> lockReturn(int returnId) {
        return jdbcTemplate.query(LOCK_RETURN, (rs, rowNum) -> new ReturnRecord(
                rs.getInt("ReturnID"),
                rs.getInt("ProductID"),
                rs.getInt("Quantity"),
                rs.getString("Status"),
                rs.getBigDecimal("RefundAmount")
        ), returnId).stream().findFirst();
    }

    public void rejectReturn(int returnId, String rejectReason) {
        jdbcTemplate.update("UPDATE Returns SET Status = 'Rejected', RejectReason = ? WHERE ReturnID = ?",
                rejectReason, returnId);
    }

    public void updateReturnStatus(int returnId, String status) {
        jdbcTemplate.update("UPDATE Returns SET Status = ? WHERE ReturnID = ?", status, returnId);
    }

    public void markReturnRefunded(int returnId, BigDecimal refundAmount) {
        jdbcTemplate.update("UPDATE Returns SET Status = 'Refunded', RefundAmount = ? WHERE ReturnID = ?",
                refundAmount, returnId);
    }

    public void increaseStock(int productId, int quantity) {
        jdbcTemplate.update(
                "UPDATE Inventory SET QuantityInStock = QuantityInStock + ?, LastUpdated = GETDATE() WHERE ProductID = ?",
                quantity, productId);
    }

    public Optional<Integer> findChosenShippingCompany(int orderId) {
        return jdbcTemplate.query("SELECT ShippingCompanyID FROM Orders WHERE OrderID = ? AND ShippingCompanyID IS NOT NULL",
                (rs, rowNum) -> rs.getInt("ShippingCompanyID"), orderId).stream().findFirst();
    }
}
