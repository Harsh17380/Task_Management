package com.TaskManager.Taskmanager.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.TaskManager.Taskmanager.dto.SupervisorTaskDTO;
import com.TaskManager.Taskmanager.model.Task;

import java.util.List;

@Repository
public class TaskRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int createTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, assigned_to, created_by, status, due_date, priority) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getCreatedBy(),
                task.getStatus(),
                task.getDueDate(),
                task.getPriority());
    }

    public List<Task> findTasksByTL(int tlId) {
        String sql = "SELECT * FROM tasks WHERE assigned_to = ? ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, id DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTask(rs), tlId);
    }

    public int updateTaskStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, taskId);
    }

    public boolean existsByIdAndAssignedTo(int taskId, int tlId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE id = ? AND assigned_to = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, tlId);
        return count != null && count > 0;
    }

    public List<SupervisorTaskDTO> findTasksBySupervisor(int supervisorId) {
        String sql = """
                SELECT t.id, t.title, t.description, t.assigned_to, u.name AS assigned_to_name,
                       t.created_by, t.status, t.due_date, t.priority
                FROM tasks t
                JOIN users u ON t.assigned_to = u.id
                WHERE t.created_by = ?
                ORDER BY t.id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SupervisorTaskDTO task = new SupervisorTaskDTO();
            task.setId(rs.getInt("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setAssignedTo(rs.getInt("assigned_to"));
            task.setAssignedToName(rs.getString("assigned_to_name"));
            task.setCreatedBy(rs.getInt("created_by"));
            task.setStatus(rs.getString("status"));
            task.setDueDate(rs.getObject("due_date", java.time.LocalDate.class));
            task.setPriority(rs.getString("priority"));
            return task;
        }, supervisorId);
    }

    private Task mapTask(java.sql.ResultSet rs) throws java.sql.SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setAssignedTo(rs.getInt("assigned_to"));
        task.setCreatedBy(rs.getInt("created_by"));
        task.setStatus(rs.getString("status"));
        task.setDueDate(rs.getObject("due_date", java.time.LocalDate.class));
        task.setPriority(rs.getString("priority"));
        return task;
    }
}
