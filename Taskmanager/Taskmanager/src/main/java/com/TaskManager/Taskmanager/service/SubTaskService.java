package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.SubTaskRequestDTO;
import com.TaskManager.Taskmanager.model.SubTask;
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

    public ApiResponse<Void> createSubTask(SubTaskRequestDTO dto) {

        if (dto.getTaskId() <= 0) {
            return new ApiResponse<>(false, "Task is required");
        }

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ApiResponse<>(false, "Subtask title is required");
        }

        if (!userRepository.existsByIdAndRole(dto.getTlId(), "TL")) {
            return new ApiResponse<>(false, "Invalid TL ID");
        }

        if (!taskRepository.existsByIdAndAssignedTo(dto.getTaskId(), dto.getTlId())) {
            return new ApiResponse<>(false, "Task is not assigned to this TL");
        }

        if (!userRepository.existsByIdAndRole(dto.getAssignedTo(), "DEVELOPER")) {
            return new ApiResponse<>(false, "Invalid Developer ID");
        }

        SubTask subTask = new SubTask();
        subTask.setTaskId(dto.getTaskId());
        subTask.setTitle(dto.getTitle().trim());
        subTask.setAssignedTo(dto.getAssignedTo());
        subTask.setStatus("PENDING");

        subTaskRepository.createSubTask(subTask);
        updateParentTaskStatus(dto.getTaskId());
        return new ApiResponse<>(true, "Subtask created and assigned to Developer");
    }

    public List<SubTask> getSubTasksForDeveloper(int devId) {
        return subTaskRepository.findByDeveloper(devId);
    }

    public ApiResponse<Void> updateStatus(int subTaskId, String status) {

        if (status == null) {
            return new ApiResponse<>(false, "Status is required");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!"PENDING".equals(normalizedStatus)
                && !"IN_PROGRESS".equals(normalizedStatus)
                && !"DONE".equals(normalizedStatus)) {
            return new ApiResponse<>(false, "Invalid status. Allowed values: PENDING, IN_PROGRESS, DONE");
        }

        int taskId = subTaskRepository.findTaskIdBySubTaskId(subTaskId);

        subTaskRepository.updateStatus(subTaskId, normalizedStatus);
        updateParentTaskStatus(taskId);

        return new ApiResponse<>(true, "Status updated");
    }

    private void updateParentTaskStatus(int taskId) {
        int total = subTaskRepository.countSubTasksByTaskId(taskId);

        if (total == 0) {
            taskRepository.updateTaskStatus(taskId, "PENDING");
            return;
        }

        int done = subTaskRepository.countSubTasksByTaskIdAndStatus(taskId, "DONE");
        int inProgress = subTaskRepository.countSubTasksByTaskIdAndStatus(taskId, "IN_PROGRESS");

        if (done == total) {
            taskRepository.updateTaskStatus(taskId, "COMPLETED");
        } else if (done > 0 || inProgress > 0) {
            taskRepository.updateTaskStatus(taskId, "IN_PROGRESS");
        } else {
            taskRepository.updateTaskStatus(taskId, "PENDING");
        }
    }
}
