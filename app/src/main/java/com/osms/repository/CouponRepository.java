package com.osms.repository;

import com.osms.model.Coupon;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CouponRepository {

    /* UPDLOCK keeps the coupon row locked until the transaction ends,
       so two orders cannot take the last allowed use of a coupon. */
    private static final String LOCK_BY_CODE = """
            SELECT CouponID, Code, DiscountType, DiscountValue, MinOrderAmount, UsageLimit, UsedCount, IsActive,
                   CASE WHEN CAST(GETDATE() AS DATE) BETWEEN StartDate AND EndDate THEN 1 ELSE 0 END AS InDateRange
            FROM Coupons WITH (UPDLOCK, ROWLOCK)
            WHERE Code = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public CouponRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Coupon> lockByCode(String code) {
        return jdbcTemplate.query(LOCK_BY_CODE, (rs, rowNum) -> new Coupon(
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
}
