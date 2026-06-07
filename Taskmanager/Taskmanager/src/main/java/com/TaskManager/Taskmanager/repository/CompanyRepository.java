package com.TaskManager.Taskmanager.repository;

import com.TaskManager.Taskmanager.model.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class CompanyRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int create(String name) {
        String sql = "INSERT INTO companies (name, status) VALUES (?, true)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM companies WHERE LOWER(name) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    public Optional<Company> findById(int id) {
        String sql = "SELECT id, name, status FROM companies WHERE id = ?";
        List<Company> companies = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Company company = new Company();
            company.setId(rs.getInt("id"));
            company.setName(rs.getString("name"));
            company.setStatus(rs.getBoolean("status"));
            return company;
        }, id);
        return companies.stream().findFirst();
    }
}
