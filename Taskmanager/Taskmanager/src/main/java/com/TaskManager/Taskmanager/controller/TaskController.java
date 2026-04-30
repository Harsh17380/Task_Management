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
    public ResponseEntity<ApiResponse> createTask(@RequestBody TaskRequestDTO dto) {
        ApiResponse response = taskService.createTask(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tl/{tlId}")
    public List<Task> getTasksForTL(@PathVariable int tlId) {
        return taskService.getTasksForTL(tlId);
    }
}
