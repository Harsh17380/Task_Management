package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.SubTaskRequestDTO;
import com.TaskManager.Taskmanager.model.SubTask;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.repository.SubTaskRepository;
import com.TaskManager.Taskmanager.repository.TaskRepository;
import com.TaskManager.Taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubTaskService {

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    public ApiResponse createSubTask(SubTaskRequestDTO dto) {

        if (dto.getTaskId() <= 0) {
            return new ApiResponse(false, "Task is required");
        }

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ApiResponse(false, "Subtask title is required");
        }

        // Validate Developer
        List<User> devs = userRepository.findByRole("DEVELOPER");

        boolean isValidDev = devs.stream()
                .anyMatch(user -> user.getId() == dto.getAssignedTo());

        if (!isValidDev) {
            return new ApiResponse(false, "Invalid Developer ID");
        }

        SubTask subTask = new SubTask();
        subTask.setTaskId(dto.getTaskId());
        subTask.setTitle(dto.getTitle().trim());
        subTask.setAssignedTo(dto.getAssignedTo());
        subTask.setStatus("PENDING");

        subTaskRepository.createSubTask(subTask);
        return new ApiResponse(true, "Subtask created and assigned to Developer");
    }

    public List<SubTask> getSubTasksForDeveloper(int devId) {
        return subTaskRepository.findByDeveloper(devId);
    }

    public ApiResponse updateStatus(int subTaskId, String status) {

        if (status == null) {
            return new ApiResponse(false, "Status is required");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!"PENDING".equals(normalizedStatus) && !"DONE".equals(normalizedStatus)) {
            return new ApiResponse(false, "Invalid status. Allowed values: PENDING, DONE");
        }

        int taskId = subTaskRepository.findTaskIdBySubTaskId(subTaskId);

        subTaskRepository.updateStatus(subTaskId, normalizedStatus);

        int remaining = subTaskRepository.countIncompleteSubTasks(taskId);

        if (remaining == 0) {
            taskRepository.updateTaskStatus(taskId, "COMPLETED");
        } else {
            taskRepository.updateTaskStatus(taskId, "IN_PROGRESS");
        }

        return new ApiResponse(true, "Status updated");
    }
}
