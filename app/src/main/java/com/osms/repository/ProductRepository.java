package com.osms.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countProducts() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Products", Integer.class);
        return count == null ? 0 : count;
    }
}
