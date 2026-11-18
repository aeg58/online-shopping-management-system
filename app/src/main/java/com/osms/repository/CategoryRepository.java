package com.osms.repository;

import com.osms.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        return jdbcTemplate.query(
                "SELECT CategoryID, CategoryName FROM Categories ORDER BY CategoryName",
                (rs, rowNum) -> new Category(rs.getInt("CategoryID"), rs.getString("CategoryName")));
    }
}
