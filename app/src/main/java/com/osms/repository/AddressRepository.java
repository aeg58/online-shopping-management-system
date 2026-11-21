package com.osms.repository;

import com.osms.model.AddressView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AddressRepository {

    private static final String SELECT_ADDRESS = """
            SELECT a.AddressID, a.Title, a.Country, a.City, a.District, a.AddressLine, a.PostalCode, a.IsDefault,
                   (SELECT COUNT(*) FROM Orders o WHERE o.AddressID = a.AddressID) AS OrderCount
            FROM Addresses a
            """;

    private static final RowMapper<AddressView> ADDRESS_MAPPER = (rs, rowNum) -> new AddressView(
            rs.getInt("AddressID"),
            rs.getString("Title"),
            rs.getString("Country"),
            rs.getString("City"),
            rs.getString("District"),
            rs.getString("AddressLine"),
            rs.getString("PostalCode"),
            rs.getBoolean("IsDefault"),
            rs.getInt("OrderCount") > 0
    );

    private final JdbcTemplate jdbcTemplate;

    public AddressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AddressView> findByCustomer(int customerId) {
        return jdbcTemplate.query(SELECT_ADDRESS + " WHERE a.CustomerID = ? AND a.IsActive = 1"
                        + " ORDER BY a.IsDefault DESC, a.AddressID",
                ADDRESS_MAPPER, customerId);
    }

    public Optional<AddressView> findForCustomer(int addressId, int customerId) {
        return jdbcTemplate.query(SELECT_ADDRESS + " WHERE a.AddressID = ? AND a.CustomerID = ? AND a.IsActive = 1",
                ADDRESS_MAPPER, addressId, customerId).stream().findFirst();
    }

    public boolean belongsToCustomer(int addressId, int customerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Addresses WHERE AddressID = ? AND CustomerID = ? AND IsActive = 1",
                Integer.class, addressId, customerId);
        return count != null && count > 0;
    }

    public void insert(int customerId, String title, String country, String city, String district,
                       String addressLine, String postalCode, boolean defaultAddress) {
        jdbcTemplate.update("""
                INSERT INTO Addresses (CustomerID, Title, Country, City, District, AddressLine, PostalCode, IsDefault)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, customerId, title, country, city, district, addressLine, postalCode, defaultAddress);
    }

    public int update(int addressId, int customerId, String title, String country, String city, String district,
                      String addressLine, String postalCode, boolean defaultAddress) {
        return jdbcTemplate.update("""
                UPDATE Addresses
                SET Title = ?, Country = ?, City = ?, District = ?, AddressLine = ?, PostalCode = ?, IsDefault = ?
                WHERE AddressID = ? AND CustomerID = ?
                """, title, country, city, district, addressLine, postalCode, defaultAddress, addressId, customerId);
    }

    public boolean isUsedByOrder(int addressId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Orders WHERE AddressID = ?",
                Integer.class, addressId);
        return count != null && count > 0;
    }

    /** The row stays for the orders that use it but is no longer shown or offered at checkout. */
    public void deactivate(int addressId, int customerId) {
        jdbcTemplate.update("UPDATE Addresses SET IsActive = 0, IsDefault = 0 WHERE AddressID = ? AND CustomerID = ?",
                addressId, customerId);
    }

    /** Only one default address per customer: the others lose the mark. */
    public void clearDefault(int customerId) {
        jdbcTemplate.update("UPDATE Addresses SET IsDefault = 0 WHERE CustomerID = ?", customerId);
    }

    /** Deletes the address only if no order ships to it. Returns the number of deleted rows. */
    public int deleteUnused(int addressId, int customerId) {
        return jdbcTemplate.update("""
                DELETE FROM Addresses
                WHERE AddressID = ? AND CustomerID = ?
                  AND NOT EXISTS (SELECT 1 FROM Orders o WHERE o.AddressID = Addresses.AddressID)
                """, addressId, customerId);
    }
}
