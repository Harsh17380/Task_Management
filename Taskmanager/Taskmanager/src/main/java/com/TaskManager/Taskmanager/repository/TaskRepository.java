package com.TaskManager.Taskmanager.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.TaskManager.Taskmanager.dto.SupervisorTaskDTO;
import com.TaskManager.Taskmanager.model.Task;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class TaskRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int createTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, assigned_to, created_by, status, due_date, priority, company_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getAssignedTo());
            ps.setInt(4, task.getCreatedBy());
            ps.setString(5, task.getStatus());
            if (task.getDueDate() == null) {
                ps.setObject(6, null);
            } else {
                ps.setObject(6, task.getDueDate());
            }
            ps.setString(7, task.getPriority());
            ps.setInt(8, task.getCompanyId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    public List<Task> findTasksByTL(int tlId, int companyId) {
        String sql = "SELECT * FROM tasks WHERE assigned_to = ? AND company_id = ? ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, id DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTask(rs), tlId, companyId);
    }

    public int updateTaskStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, taskId);
    }

    public String findStatusById(int taskId) {
        String sql = "SELECT status FROM tasks WHERE id = ?";
        List<String> statuses = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("status"), taskId);
        return statuses.isEmpty() ? null : statuses.get(0);
    }

    public boolean existsByIdAndAssignedTo(int taskId, int tlId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE id = ? AND assigned_to = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, tlId);
        return count != null && count > 0;
    }

    public boolean existsByIdAndAssignedToAndCompany(int taskId, int tlId, int companyId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE id = ? AND assigned_to = ? AND company_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, tlId, companyId);
        return count != null && count > 0;
    }

    public boolean existsByIdAndCreatedBy(int taskId, int supervisorId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE id = ? AND created_by = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, supervisorId);
        return count != null && count > 0;
    }

    public List<SupervisorTaskDTO> findTasksBySupervisor(int supervisorId, int companyId) {
        String sql = """
                SELECT t.id, t.title, t.description, t.assigned_to, u.name AS assigned_to_name,
                       t.created_by, t.status, t.due_date, t.priority
                FROM tasks t
                JOIN users u ON t.assigned_to = u.id
                WHERE t.created_by = ? AND t.company_id = ?
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
        }, supervisorId, companyId);
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
        task.setCompanyId(rs.getInt("company_id"));
        return task;
    }
}
