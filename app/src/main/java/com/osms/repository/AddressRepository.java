package com.osms.repository;

import com.osms.model.AddressView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AddressRepository {

    private static final String FIND_BY_CUSTOMER = """
            SELECT AddressID, Title, City, District, AddressLine, IsDefault
            FROM Addresses
            WHERE CustomerID = ?
            ORDER BY IsDefault DESC, AddressID
            """;

    private final JdbcTemplate jdbcTemplate;

    public AddressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AddressView> findByCustomer(int customerId) {
        return jdbcTemplate.query(FIND_BY_CUSTOMER, (rs, rowNum) -> new AddressView(
                rs.getInt("AddressID"),
                rs.getString("Title"),
                rs.getString("City"),
                rs.getString("District"),
                rs.getString("AddressLine"),
                rs.getBoolean("IsDefault")
        ), customerId);
    }

    public boolean belongsToCustomer(int addressId, int customerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Addresses WHERE AddressID = ? AND CustomerID = ?",
                Integer.class, addressId, customerId);
        return count != null && count > 0;
    }
}
