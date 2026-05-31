package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.SubTaskRequestDTO;
import com.TaskManager.Taskmanager.model.SubTask;
import com.TaskManager.Taskmanager.service.SubTaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subtasks")
public class SubTaskController {

    @Autowired
    private SubTaskService subTaskService;

    // TL creates subtask
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createSubTask(@RequestBody SubTaskRequestDTO dto) {
        ApiResponse<Void> response = subTaskService.createSubTask(dto);
        return ResponseEntity.ok(response);
    }

    // Developer views subtasks
    @GetMapping("/dev/{devId}")
    public ResponseEntity<ApiResponse<List<SubTask>>> getSubTasks(@PathVariable int devId) {
        List<SubTask> subTasks = subTaskService.getSubTasksForDeveloper(devId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Subtasks fetched successfully", subTasks));
    }

    // Developer updates status
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable int id,
            @RequestParam String status,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        ApiResponse<Void> response = subTaskService.updateStatus(id, status, userId == null ? 0 : userId);
        return ResponseEntity.ok(response);
    }

}
