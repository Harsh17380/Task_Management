package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.LoginRequestDTO;
import com.TaskManager.Taskmanager.dto.LoginResponseDTO;
import com.TaskManager.Taskmanager.dto.UserRequestDTO;
import com.TaskManager.Taskmanager.dto.UserResponseDTO;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Register user — requires SUPERVISOR role (enforced by SecurityConfig)
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUser(@RequestBody UserRequestDTO dto) {
        ApiResponse<Void> response = userService.createUser(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO dto) {
        ApiResponse<LoginResponseDTO> response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable String role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users fetched successfully", users));
    }
}
