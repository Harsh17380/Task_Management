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

    @Autowired
    private NotificationService notificationService;

    private static final List<String> ALLOWED_PRIORITIES =
            List.of("LOW", "MEDIUM", "HIGH", "URGENT");

    public ApiResponse<Void> createTask(TaskRequestDTO dto, int actorUserId, Integer companyId) {

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ApiResponse<>(false, "Task title is required");
        }

        if (companyId == null || companyId <= 0) {
            return new ApiResponse<>(false, "Company account is not configured");
        }

        if (!userRepository.existsByIdAndRoleAndCompany(actorUserId, "SUPERVISOR", companyId)) {
            return new ApiResponse<>(false, "Invalid Supervisor ID");
        }

        if (!userRepository.existsByIdAndRoleAndCompany(dto.getAssignedTo(), "TL", companyId)) {
            return new ApiResponse<>(false, "Selected TL does not belong to your company");
        }

        String priority = normalizePriority(dto.getPriority());
        if (priority == null) {
            return new ApiResponse<>(false, "Invalid priority");
        }

        Task task = new Task();
        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription());
        task.setAssignedTo(dto.getAssignedTo());
        task.setCreatedBy(actorUserId);
        task.setStatus("PENDING");
        task.setDueDate(dto.getDueDate());
        task.setPriority(priority);
        task.setCompanyId(companyId);

        int taskId = taskRepository.createTask(task);
        if (taskId > 0) {
            String tlName = userRepository.findNameById(dto.getAssignedTo());
            taskCommentRepository.saveActivity(
                    taskId,
                    actorUserId,
                    "Task created and assigned to " + tlName
            );
            // Notify the assigned TL
            notificationService.push(
                    dto.getAssignedTo(),
                    companyId,
                    "TASK_ASSIGNED",
                    "New task assigned to you: \"" + task.getTitle() + "\"",
                    taskId
            );
        }
        return new ApiResponse<>(true, "Task created and assigned to TL");
    }

    public List<Task> getTasksForTL(int tlId, int actorUserId, Integer companyId) {
        if (companyId == null || tlId != actorUserId) {
            return List.of();
        }
        return taskRepository.findTasksByTL(tlId, companyId);
    }

    public List<SupervisorTaskDTO> getTasksForSupervisor(int supervisorId, int actorUserId, Integer companyId) {
        if (companyId == null || supervisorId != actorUserId) {
            return List.of();
        }
        return taskRepository.findTasksBySupervisor(supervisorId, companyId);
    }

    private String normalizePriority(String priority) {
        String normalized = priority == null || priority.trim().isEmpty()
                ? "MEDIUM"
                : priority.trim().toUpperCase();
        return ALLOWED_PRIORITIES.contains(normalized) ? normalized : null;
    }
}
