package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.CommentRequestDTO;
import com.TaskManager.Taskmanager.model.TaskComment;
import com.TaskManager.Taskmanager.service.TaskCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@CrossOrigin
public class TaskCommentController {

    @Autowired
    private TaskCommentService service;

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<ApiResponse<Void>> addComment(
            @PathVariable int taskId,
            @RequestBody CommentRequestDTO dto,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null || role == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Login session is missing. Please login again."));
        }

        ApiResponse<Void> response = service.addComment(taskId, userId, role, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<ApiResponse<List<TaskComment>>> getComments(
            @PathVariable int taskId,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null || role == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Login session is missing. Please login again.", List.of()));
        }

        ApiResponse<List<TaskComment>> response = service.getComments(taskId, userId, role);
        return ResponseEntity.ok(response);
    }
}
