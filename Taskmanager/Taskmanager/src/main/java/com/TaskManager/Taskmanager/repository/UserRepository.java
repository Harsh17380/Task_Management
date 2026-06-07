package com.TaskManager.Taskmanager.repository;

import com.TaskManager.Taskmanager.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int save(User user) {
        String sql = "INSERT INTO users (name, email, password, role, status, company_id) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                true,
                user.getCompanyId());
    }

    public List<User> findByRoleAndCompany(String role, int companyId) {
        String sql = """
                SELECT u.*, c.name AS company_name
                FROM users u
                JOIN companies c ON u.company_id = c.id
                WHERE u.role = ? AND u.company_id = ? AND u.status = true AND c.status = true
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), role, companyId);
    }

    public List<User> findByRole(String role) {
        String sql = """
                SELECT u.*, c.name AS company_name
                FROM users u
                LEFT JOIN companies c ON u.company_id = c.id
                WHERE u.role = ? AND u.status = true
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), role);
    }

    public boolean existsByIdAndRoleAndCompany(int id, String role, int companyId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND role = ? AND company_id = ? AND status = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, role, companyId);
        return count != null && count > 0;
    }

    public Optional<User> findByEmailAndPassword(String email, String password) {
        String sql = """
                SELECT u.*, c.name AS company_name
                FROM users u
                LEFT JOIN companies c ON u.company_id = c.id
                WHERE u.email = ? AND u.password = ? AND u.status = true
                """;
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), email, password);

        return users.stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT u.*, c.name AS company_name
                FROM users u
                LEFT JOIN companies c ON u.company_id = c.id
                WHERE LOWER(u.email) = LOWER(?)
                """;
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), email);

        return users.stream().findFirst();
    }

    public int updatePassword(int userId, String newPassword) {

        String sql = "UPDATE users SET password = ? WHERE id = ? AND status = true";

        return jdbcTemplate.update(sql, newPassword, userId);
    }

    public Optional<User> findById(int id) {

        String sql = """
                SELECT u.*, c.name AS company_name
                FROM users u
                LEFT JOIN companies c ON u.company_id = c.id
                WHERE u.id = ?
                """;

        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), id);

        return users.stream().findFirst();
    }
    public int deleteUserById(int id) {

        String sql = "UPDATE users SET status = false WHERE id = ? AND status = true";

        return jdbcTemplate.update(sql, id);
    }

    public List<User> findAll() {
        String sql = """
                SELECT u.*, c.name AS company_name
                FROM users u
                LEFT JOIN companies c ON u.company_id = c.id
                ORDER BY c.name NULLS FIRST, u.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs));
    }

    public List<User> findAllByCompany(int companyId) {
        String sql = """
                SELECT u.*, c.name AS company_name
                FROM users u
                JOIN companies c ON u.company_id = c.id
                WHERE u.company_id = ?
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), companyId);
    }

    public String findNameById(int id) {
        String sql = "SELECT name FROM users WHERE id = ?";
        List<String> names = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), id);
        return names.isEmpty() ? "User #" + id : names.get(0);
    }

    private User mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getBoolean("status"));
        Object companyId = rs.getObject("company_id");
        user.setCompanyId(companyId == null ? null : ((Number) companyId).intValue());
        user.setCompanyName(rs.getString("company_name"));
        return user;
    }
}
