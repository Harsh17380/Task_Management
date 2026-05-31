package com.TaskManager.Taskmanager.repository;

import com.TaskManager.Taskmanager.model.TaskComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskCommentRepository {

    private static final String ACTIVITY_PREFIX = "[ACTIVITY] ";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int save(TaskComment comment) {

        String sql = """
            INSERT INTO task_comments
            (task_id, user_id, "comment")
            VALUES (?, ?, ?)
        """;

        return jdbcTemplate.update(
                sql,
                comment.getTaskId(),
                comment.getUserId(),
                comment.getComment()
        );
    }

    public int saveActivity(int taskId, int userId, String activityText) {
        if (userId <= 0) {
            return 0;
        }

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setComment(ACTIVITY_PREFIX + activityText);
        return save(comment);
    }

    public List<TaskComment> getCommentsByTaskId(int taskId) {

        String sql = """
            SELECT tc.id, tc.task_id, tc.user_id, u.name AS user_name, u.role AS user_role,
                   tc."comment" AS comment_text, tc.created_at
            FROM task_comments tc
            JOIN users u ON tc.user_id = u.id
            WHERE tc.task_id = ?
            ORDER BY tc.created_at ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            TaskComment comment = new TaskComment();

            comment.setId(rs.getInt("id"));
            comment.setTaskId(rs.getInt("task_id"));
            comment.setUserId(rs.getInt("user_id"));
            comment.setUserName(rs.getString("user_name"));
            comment.setUserRole(rs.getString("user_role"));
            String text = rs.getString("comment_text");
            if (text != null && text.startsWith(ACTIVITY_PREFIX)) {
                comment.setCommentType("ACTIVITY");
                comment.setComment(text.substring(ACTIVITY_PREFIX.length()));
            } else {
                comment.setCommentType("COMMENT");
                comment.setComment(text);
            }
            comment.setCreatedAt(rs.getTimestamp("created_at"));

            return comment;

        }, taskId);
    }
}
