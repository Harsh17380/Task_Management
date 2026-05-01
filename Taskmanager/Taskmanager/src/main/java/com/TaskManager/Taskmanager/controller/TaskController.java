package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.TaskRequestDTO;
import com.TaskManager.Taskmanager.model.Task;
import com.TaskManager.Taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createTask(@RequestBody TaskRequestDTO dto) {
        ApiResponse<Void> response = taskService.createTask(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tl/{tlId}")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksForTL(@PathVariable int tlId) {
        List<Task> tasks = taskService.getTasksForTL(tlId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tasks fetched successfully", tasks));
    }
}
