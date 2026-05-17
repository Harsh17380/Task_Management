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
        String sql = "INSERT INTO users (name, email, password, role, status) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                true);
    }

    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ? AND status = true";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), role);
    }

    public boolean existsByIdAndRole(int id, String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND role = ? AND status = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, role);
        return count != null && count > 0;
    }

    public Optional<User> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND status = true";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), email, password);

        return users.stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), email);

        return users.stream().findFirst();
    }

    public int updatePassword(int userId, String newPassword) {

        String sql = "UPDATE users SET password = ? WHERE id = ? AND status = true";

        return jdbcTemplate.update(sql, newPassword, userId);
    }

    public Optional<User> findById(int id) {

        String sql = "SELECT * FROM users WHERE id = ?";

        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), id);

        return users.stream().findFirst();
    }
    public int deleteUserById(int id) {

        String sql = "UPDATE users SET status = false WHERE id = ? AND status = true";

        return jdbcTemplate.update(sql, id);
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs));
    }

    private User mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getBoolean("status"));
        return user;
    }
}
