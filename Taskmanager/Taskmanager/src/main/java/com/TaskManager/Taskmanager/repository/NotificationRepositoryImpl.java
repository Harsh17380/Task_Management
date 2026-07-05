package com.TaskManager.Taskmanager.repository;

import com.TaskManager.Taskmanager.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void create(Notification n) {
        String sql = """
                INSERT INTO notifications (user_id, company_id, message, type, reference_id, is_read)
                VALUES (?, ?, ?, ?, ?, false)
                """;
        jdbcTemplate.update(sql,
                n.getUserId(),
                n.getCompanyId() == 0 ? null : n.getCompanyId(),
                n.getMessage(),
                n.getType(),
                n.getReferenceId());
    }

    @Override
    public List<Notification> getUserNotifications(int userId) {
        String sql = """
                SELECT id, user_id, company_id, message, type, reference_id, is_read, created_at
                FROM notifications
                WHERE user_id = ?
                ORDER BY created_at DESC
                LIMIT 50
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setUserId(rs.getInt("user_id"));
            Object cid = rs.getObject("company_id");
            n.setCompanyId(cid == null ? 0 : ((Number) cid).intValue());
            n.setMessage(rs.getString("message"));
            n.setType(rs.getString("type"));
            Object rid = rs.getObject("reference_id");
            n.setReferenceId(rid == null ? null : ((Number) rid).intValue());
            n.setRead(rs.getBoolean("is_read"));
            n.setCreatedAt(rs.getTimestamp("created_at"));
            return n;
        }, userId);
    }

    @Override
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count == null ? 0 : count;
    }

    @Override
    public int markAsRead(int notificationId, int userId) {
        return jdbcTemplate.update(
                "UPDATE notifications SET is_read = true WHERE id = ? AND user_id = ?",
                notificationId,
                userId);
    }

    @Override
    public void markAllAsRead(int userId) {
        jdbcTemplate.update(
                "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false",
                userId);
    }
}
