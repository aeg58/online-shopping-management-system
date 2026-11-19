package com.osms.repository;

import com.osms.model.ShipmentView;
import com.osms.model.ShippingCompany;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ShipmentRepository {

    private static final String FIND_BY_ORDER = """
            SELECT sc.CompanyName, sh.TrackingNumber, sh.Status, sh.ShippedDate, sh.DeliveredDate
            FROM Shipments sh
            JOIN ShippingCompanies sc ON sc.ShippingCompanyID = sh.ShippingCompanyID
            WHERE sh.OrderID = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public ShipmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ShipmentView> findByOrder(int orderId) {
        return jdbcTemplate.query(FIND_BY_ORDER, (rs, rowNum) -> new ShipmentView(
                rs.getString("CompanyName"),
                rs.getString("TrackingNumber"),
                rs.getString("Status"),
                toLocalDate(rs.getDate("ShippedDate")),
                toLocalDate(rs.getDate("DeliveredDate"))
        ), orderId).stream().findFirst();
    }

    public List<ShippingCompany> findCompanies() {
        return jdbcTemplate.query(
                "SELECT ShippingCompanyID, CompanyName FROM ShippingCompanies ORDER BY CompanyName",
                (rs, rowNum) -> new ShippingCompany(rs.getInt("ShippingCompanyID"), rs.getString("CompanyName")));
    }

    public void insertInTransit(int orderId, int shippingCompanyId, String trackingNumber) {
        jdbcTemplate.update("""
                INSERT INTO Shipments (OrderID, ShippingCompanyID, TrackingNumber, Status, ShippedDate)
                VALUES (?, ?, ?, 'InTransit', CAST(GETDATE() AS DATE))
                """, orderId, shippingCompanyId, trackingNumber);
    }

    public int markDelivered(int orderId) {
        return jdbcTemplate.update("""
                UPDATE Shipments
                SET Status = 'Delivered', DeliveredDate = CAST(GETDATE() AS DATE)
                WHERE OrderID = ? AND Status <> 'Delivered'
                """, orderId);
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
