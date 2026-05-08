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
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole());
    }

    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        return jdbcTemplate.query(sql, new Object[]{role}, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            return user;
        });
    }

    public boolean existsByIdAndRole(int id, String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND role = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, role);
        return count != null && count > 0;
    }

    public Optional<User> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{email, password}, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            return user;
        });

        return users.stream().findFirst();
    }
}
