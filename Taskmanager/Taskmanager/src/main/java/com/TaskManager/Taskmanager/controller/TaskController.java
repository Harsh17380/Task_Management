package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.SupervisorTaskDTO;
import com.TaskManager.Taskmanager.dto.TaskRequestDTO;
import com.TaskManager.Taskmanager.model.Task;
import com.TaskManager.Taskmanager.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createTask(@RequestBody TaskRequestDTO dto, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer companyId = (Integer) request.getAttribute("companyId");
        ApiResponse<Void> response = taskService.createTask(dto, userId == null ? 0 : userId, companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tl/{tlId}")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksForTL(@PathVariable int tlId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer companyId = (Integer) request.getAttribute("companyId");
        List<Task> tasks = taskService.getTasksForTL(tlId, userId == null ? 0 : userId, companyId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tasks fetched successfully", tasks));
    }

    @GetMapping("/supervisor/{supervisorId}")
    public ResponseEntity<ApiResponse<List<SupervisorTaskDTO>>> getTasksForSupervisor(
            @PathVariable int supervisorId,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer companyId = (Integer) request.getAttribute("companyId");
        List<SupervisorTaskDTO> tasks =
                taskService.getTasksForSupervisor(supervisorId, userId == null ? 0 : userId, companyId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Supervisor tasks fetched successfully", tasks));
    }
}
