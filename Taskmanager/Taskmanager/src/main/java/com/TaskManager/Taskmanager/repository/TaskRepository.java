package com.TaskManager.Taskmanager.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.TaskManager.Taskmanager.model.Task;

import java.util.List;

@Repository
public class TaskRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int createTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, assigned_to, created_by, status) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getCreatedBy(),
                task.getStatus());
    }

    public List<Task> findTasksByTL(int tlId) {
        String sql = "SELECT * FROM tasks WHERE assigned_to = ?";
        return jdbcTemplate.query(sql, new Object[]{tlId}, (rs, rowNum) -> {
            Task task = new Task();
            task.setId(rs.getInt("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setAssignedTo(rs.getInt("assigned_to"));
            task.setCreatedBy(rs.getInt("created_by"));
            task.setStatus(rs.getString("status"));
            return task;
        });
    }

    public int updateTaskStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, taskId);
    }


}
