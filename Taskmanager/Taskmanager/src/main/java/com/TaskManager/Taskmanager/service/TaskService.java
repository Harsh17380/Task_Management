package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.SupervisorTaskDTO;
import com.TaskManager.Taskmanager.dto.TaskRequestDTO;
import com.TaskManager.Taskmanager.model.Task;
import com.TaskManager.Taskmanager.repository.TaskCommentRepository;
import com.TaskManager.Taskmanager.repository.TaskRepository;
import com.TaskManager.Taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    private static final List<String> ALLOWED_PRIORITIES =
            List.of("LOW", "MEDIUM", "HIGH", "URGENT");

    public ApiResponse<Void> createTask(TaskRequestDTO dto) {

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ApiResponse<>(false, "Task title is required");
        }

        if (!userRepository.existsByIdAndRole(dto.getCreatedBy(), "SUPERVISOR")) {
            return new ApiResponse<>(false, "Invalid Supervisor ID");
        }

        if (!userRepository.existsByIdAndRole(dto.getAssignedTo(), "TL")) {
            return new ApiResponse<>(false, "Invalid TL ID");
        }

        String priority = normalizePriority(dto.getPriority());
        if (priority == null) {
            return new ApiResponse<>(false, "Invalid priority");
        }

        Task task = new Task();
        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription());
        task.setAssignedTo(dto.getAssignedTo());
        task.setCreatedBy(dto.getCreatedBy());
        task.setStatus("PENDING");
        task.setDueDate(dto.getDueDate());
        task.setPriority(priority);

        int taskId = taskRepository.createTask(task);
        if (taskId > 0) {
            String tlName = userRepository.findNameById(dto.getAssignedTo());
            taskCommentRepository.saveActivity(
                    taskId,
                    dto.getCreatedBy(),
                    "Task created and assigned to " + tlName
            );
        }
        return new ApiResponse<>(true, "Task created and assigned to TL");
    }

    public List<Task> getTasksForTL(int tlId) {
        return taskRepository.findTasksByTL(tlId);
    }

    public List<SupervisorTaskDTO> getTasksForSupervisor(int supervisorId) {
        return taskRepository.findTasksBySupervisor(supervisorId);
    }

    private String normalizePriority(String priority) {
        String normalized = priority == null || priority.trim().isEmpty()
                ? "MEDIUM"
                : priority.trim().toUpperCase();
        return ALLOWED_PRIORITIES.contains(normalized) ? normalized : null;
    }
}
