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
        return jdbcTemplate.query(sql, new Object[]{devId}, (rs, rowNum) -> {
            SubTask st = new SubTask();
            st.setId(rs.getInt("id"));
            st.setTaskId(rs.getInt("task_id"));
            st.setTitle(rs.getString("title"));
            st.setAssignedTo(rs.getInt("assigned_to"));
            st.setStatus(rs.getString("status"));
            return st;
        });
    }

    public int updateStatus(int subTaskId, String status) {
        String sql = "UPDATE sub_tasks SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, subTaskId);
    }
    public int countIncompleteSubTasks(int taskId) {
        String sql = "SELECT COUNT(*) FROM sub_tasks WHERE task_id = ? AND status != 'DONE'";
        return jdbcTemplate.queryForObject(sql, Integer.class, taskId);
    }

    public int countSubTasksByTaskId(int taskId) {
        String sql = "SELECT COUNT(*) FROM sub_tasks WHERE task_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, taskId);
    }

    public int countSubTasksByTaskIdAndStatus(int taskId, String status) {
        String sql = "SELECT COUNT(*) FROM sub_tasks WHERE task_id = ? AND status = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, taskId, status);
    }

    public int findTaskIdBySubTaskId(int subTaskId) {
        String sql = "SELECT task_id FROM sub_tasks WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, subTaskId);
    }
}
