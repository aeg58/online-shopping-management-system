package com.osms.repository;

import com.osms.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepository {

    private static final RowMapper<Category> CATEGORY_MAPPER = (rs, rowNum) -> new Category(
            rs.getInt("CategoryID"),
            rs.getString("CategoryName"),
            rs.getString("Description")
    );

    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        return jdbcTemplate.query(
                "SELECT CategoryID, CategoryName, Description FROM Categories ORDER BY CategoryName",
                CATEGORY_MAPPER);
    }

    public Optional<Category> findById(int categoryId) {
        return jdbcTemplate.query(
                "SELECT CategoryID, CategoryName, Description FROM Categories WHERE CategoryID = ?",
                CATEGORY_MAPPER, categoryId).stream().findFirst();
    }

    public void insert(String categoryName, String description) {
        jdbcTemplate.update("INSERT INTO Categories (CategoryName, Description) VALUES (?, ?)",
                categoryName, description);
    }

    public int update(int categoryId, String categoryName, String description) {
        return jdbcTemplate.update("UPDATE Categories SET CategoryName = ?, Description = ? WHERE CategoryID = ?",
                categoryName, description, categoryId);
    }
}
