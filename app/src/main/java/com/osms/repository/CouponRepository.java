package com.osms.repository;

import com.osms.model.Coupon;
import com.osms.model.CouponRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CouponRepository {

    private static final String LOCK_BY_CODE = """
            SELECT CouponID, Code, DiscountType, DiscountValue, MinOrderAmount, UsageLimit, UsedCount, IsActive,
                   CASE WHEN CAST(GETDATE() AS DATE) BETWEEN StartDate AND EndDate THEN 1 ELSE 0 END AS InDateRange
            FROM Coupons WITH (UPDLOCK, ROWLOCK)
            WHERE Code = ?
            """;

    private static final String FIND_BY_CODE = """
            SELECT CouponID, Code, DiscountType, DiscountValue, MinOrderAmount, UsageLimit, UsedCount, IsActive,
                   CASE WHEN CAST(GETDATE() AS DATE) BETWEEN StartDate AND EndDate THEN 1 ELSE 0 END AS InDateRange
            FROM Coupons
            WHERE Code = ?
            """;

    private static final String FIND_ALL = """
            SELECT CouponID, Code, DiscountType, DiscountValue, MinOrderAmount,
                   StartDate, EndDate, UsageLimit, UsedCount, IsActive
            FROM Coupons
            ORDER BY EndDate DESC, Code
            """;

    private static final String INSERT_COUPON = """
            INSERT INTO Coupons (Code, DiscountType, DiscountValue, MinOrderAmount, StartDate, EndDate, UsageLimit)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public CouponRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Coupon> lockByCode(String code) {
        return findCoupon(LOCK_BY_CODE, code);
    }

    public Optional<Coupon> findByCode(String code) {
        return findCoupon(FIND_BY_CODE, code);
    }

    private Optional<Coupon> findCoupon(String sql, String code) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Coupon(
                rs.getInt("CouponID"),
                rs.getString("Code"),
                rs.getString("DiscountType"),
                rs.getBigDecimal("DiscountValue"),
                rs.getBigDecimal("MinOrderAmount"),
                rs.getInt("UsageLimit"),
                rs.getInt("UsedCount"),
                rs.getBoolean("IsActive"),
                rs.getInt("InDateRange") == 1
        ), code).stream().findFirst();
    }

    public void increaseUsedCount(int couponId) {
        jdbcTemplate.update("UPDATE Coupons SET UsedCount = UsedCount + 1 WHERE CouponID = ?", couponId);
    }

    public List<CouponRow> findAll() {
        return jdbcTemplate.query(FIND_ALL, (rs, rowNum) -> new CouponRow(
                rs.getInt("CouponID"),
                rs.getString("Code"),
                rs.getString("DiscountType"),
                rs.getBigDecimal("DiscountValue"),
                rs.getBigDecimal("MinOrderAmount"),
                rs.getDate("StartDate").toLocalDate(),
                rs.getDate("EndDate").toLocalDate(),
                rs.getInt("UsageLimit"),
                rs.getInt("UsedCount"),
                rs.getBoolean("IsActive")
        ));
    }

    public void insert(String code, String discountType, BigDecimal discountValue, BigDecimal minOrderAmount,
                       LocalDate startDate, LocalDate endDate, int usageLimit) {
        jdbcTemplate.update(INSERT_COUPON, code, discountType, discountValue, minOrderAmount,
                Date.valueOf(startDate), Date.valueOf(endDate), usageLimit);
    }
}
