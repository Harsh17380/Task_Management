package com.TaskManager.Taskmanager.repository;

import com.TaskManager.Taskmanager.model.SubTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubTaskRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int createSubTask(SubTask subTask) {
        String sql = "INSERT INTO sub_tasks (task_id, title, assigned_to, status) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                subTask.getTaskId(),
                subTask.getTitle(),
                subTask.getAssignedTo(),
                subTask.getStatus());
    }

    public List<SubTask> findByDeveloper(int devId) {
        String sql = "SELECT * FROM sub_tasks WHERE assigned_to = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SubTask st = new SubTask();
            st.setId(rs.getInt("id"));
            st.setTaskId(rs.getInt("task_id"));
            st.setTitle(rs.getString("title"));
            st.setAssignedTo(rs.getInt("assigned_to"));
            st.setStatus(rs.getString("status"));
            return st;
        }, devId);
    }

    public int updateStatus(int subTaskId, String status) {
        String sql = "UPDATE sub_tasks SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, subTaskId);
    }

    public String findStatusById(int subTaskId) {
        String sql = "SELECT status FROM sub_tasks WHERE id = ?";
        List<String> statuses = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("status"), subTaskId);
        return statuses.isEmpty() ? null : statuses.get(0);
    }

    public String findTitleById(int subTaskId) {
        String sql = "SELECT title FROM sub_tasks WHERE id = ?";
        List<String> titles = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("title"), subTaskId);
        return titles.isEmpty() ? "Subtask #" + subTaskId : titles.get(0);
    }

    public int countIncompleteSubTasks(int taskId) {
        String sql = "SELECT COUNT(*) FROM sub_tasks WHERE task_id = ? AND status != 'DONE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId);
        return count != null ? count : 0;
    }

    public int countSubTasksByTaskId(int taskId) {
        String sql = "SELECT COUNT(*) FROM sub_tasks WHERE task_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId);
        return count != null ? count : 0;
    }

    public int countSubTasksByTaskIdAndStatus(int taskId, String status) {
        String sql = "SELECT COUNT(*) FROM sub_tasks WHERE task_id = ? AND status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, status);
        return count != null ? count : 0;
    }

    public int findTaskIdBySubTaskId(int subTaskId) {
        String sql = "SELECT task_id FROM sub_tasks WHERE id = ?";
        Integer taskId = jdbcTemplate.queryForObject(sql, Integer.class, subTaskId);
        return taskId != null ? taskId : 0;
    }

    public boolean existsByTaskIdAndDeveloper(int taskId, int developerId) {
        String sql = "SELECT COUNT(*) FROM sub_tasks WHERE task_id = ? AND assigned_to = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, developerId);
        return count != null && count > 0;
    }
}
