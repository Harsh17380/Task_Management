package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.TaskRequestDTO;
import com.TaskManager.Taskmanager.model.Task;
import com.TaskManager.Taskmanager.model.User;
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

    public ApiResponse<Void> createTask(TaskRequestDTO dto) {

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ApiResponse<>(false, "Task title is required");
        }

        // Validate role (VERY IMPORTANT)
        List<User> tlList = userRepository.findByRole("TL");

        boolean isValidTL = tlList.stream()
                .anyMatch(user -> user.getId() == dto.getAssignedTo());

        if (!isValidTL) {
            return new ApiResponse<>(false, "Invalid TL ID");
        }

        Task task = new Task();
        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription());
        task.setAssignedTo(dto.getAssignedTo());
        task.setCreatedBy(dto.getCreatedBy());
        task.setStatus("PENDING");

        taskRepository.createTask(task);
        return new ApiResponse<>(true, "Task created and assigned to TL");
    }

    public List<Task> getTasksForTL(int tlId) {
        return taskRepository.findTasksByTL(tlId);
    }
}
