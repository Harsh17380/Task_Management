package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.service.SubTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/subtasks")
public class SubTaskController {

    @Autowired
    private SubTaskService subTaskService;

    // TL creates subtask
    @PostMapping
    public ResponseEntity<ApiResponse> createSubTask(@RequestBody SubTask subTask) {

        String message = subTaskService.createSubTask(subTask);

        return ResponseEntity.ok(
                new ApiResponse(true, message)
        );
    }
    // Developer views subtasks
    @GetMapping("/dev/{devId}")
    public List<SubTask> getSubTasks(@PathVariable int devId) {
        return subTaskService.getSubTasksForDeveloper(devId);
    }

    // Developer updates status
    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable int id, @RequestParam String status) {
        return subTaskService.updateStatus(id, status);
    }

}
