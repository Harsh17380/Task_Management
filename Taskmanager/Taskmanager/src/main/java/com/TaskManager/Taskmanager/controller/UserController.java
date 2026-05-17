package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.*;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordDTO dto,
            HttpServletRequest request
    ) {

        Integer userId =
                (Integer) request.getAttribute("userId");

        ApiResponse<Void> response =
                userService.changePassword(userId, dto);

        return ResponseEntity.ok(response);
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUsersByRole(@PathVariable String role) {
        List<User> users = userService.getUsersByRole(role);
        List<UserResponseDTO> responseData = users.stream()
                .map(UserResponseDTO::new)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Users fetched successfully", responseData));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable int id,
            HttpServletRequest request
    ) {

        Integer loggedInUserId =
                (Integer) request.getAttribute("userId");

        String loggedInRole =
                (String) request.getAttribute("role");

        String loggedInEmail =
                (String) request.getAttribute("email");

        ApiResponse<Void> response =
                userService.deleteUser(
                        loggedInUserId,
                        loggedInRole,
                        loggedInEmail,
                        id
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers(HttpServletRequest request) {

        String loggedInEmail =
                (String) request.getAttribute("email");

        ApiResponse<List<User>> usersResponse = userService.getAllUsers(loggedInEmail);

        if (!usersResponse.isSuccess()) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            false,
                            usersResponse.getMessage(),
                            List.of()
                    )
            );
        }

        List<User> users = usersResponse.getData();
        List<UserResponseDTO> responseData = users.stream()
                .map(UserResponseDTO::new)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Users fetched successfully",
                        responseData
                )
        );
    }
}
