package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.CommentRequestDTO;
import com.TaskManager.Taskmanager.model.TaskComment;
import com.TaskManager.Taskmanager.repository.SubTaskRepository;
import com.TaskManager.Taskmanager.repository.TaskCommentRepository;
import com.TaskManager.Taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskCommentService {

    @Autowired
    private TaskCommentRepository repository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubTaskRepository subTaskRepository;

    public ApiResponse<Void> addComment(
            int taskId,
            int userId,
            String role,
            CommentRequestDTO dto
    ) {

        if (!canAccessTask(taskId, userId, role)) {
            return new ApiResponse<>(false, "You are not allowed to comment on this task");
        }

        if (dto.getComment() == null || dto.getComment().trim().isEmpty()) {
            return new ApiResponse<>(false, "Comment is required");
        }

        TaskComment comment = new TaskComment();

        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setComment(dto.getComment().trim());

        repository.save(comment);

        return new ApiResponse<>(true, "Comment added successfully");
    }

    public ApiResponse<List<TaskComment>> getComments(
            int taskId,
            int userId,
            String role
    ) {

        if (!canAccessTask(taskId, userId, role)) {
            return new ApiResponse<>(false, "You are not allowed to view comments for this task", List.of());
        }

        return new ApiResponse<>(true, "Comments fetched successfully", repository.getCommentsByTaskId(taskId));
    }

    private boolean canAccessTask(int taskId, int userId, String role) {
        if ("SUPERVISOR".equals(role)) {
            return taskRepository.existsByIdAndCreatedBy(taskId, userId);
        }

        if ("TL".equals(role)) {
            return taskRepository.existsByIdAndAssignedTo(taskId, userId);
        }

        if ("DEVELOPER".equals(role)) {
            return subTaskRepository.existsByTaskIdAndDeveloper(taskId, userId);
        }

        return false;
    }
}
