package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.*;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.repository.UserRepository;
import com.TaskManager.Taskmanager.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final String ADMIN_EMAIL = "admin@corequeue.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ApiResponse<Void> createUser(UserRequestDTO dto) {

        if (dto.getRole() == null) {
            return new ApiResponse<>(false, "Role is required");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setStatus(true);

        userRepository.save(user);

        return new ApiResponse<>(true, "User created successfully");
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // UPDATED: Returns JWT token on successful login
    public ApiResponse<LoginResponseDTO> login(LoginRequestDTO dto) {

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return new ApiResponse<>(false, "Email is required");
        }

        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            return new ApiResponse<>(false, "Password is required");
        }

        Optional<User> user = userRepository.findByEmail(dto.getEmail().trim());

        if (user.isEmpty()) {
            return new ApiResponse<>(false, "Invalid email or password");
        }

        User foundUser = user.get();

        if (!passwordEncoder.matches(dto.getPassword(), foundUser.getPassword())) {
            return new ApiResponse<>(false, "Invalid email or password");
        }

        if (!foundUser.getStatus()) {
            return new ApiResponse<>(false, "User is deactivated");
        }

        String token = jwtUtil.generateToken(
                foundUser.getEmail(),
                foundUser.getId(),
                foundUser.getRole()
        );

        LoginResponseDTO responseData =
                new LoginResponseDTO(foundUser, token);

        return new ApiResponse<>(true, "Login successful", responseData);
    }

    public ApiResponse<Void> changePassword(
            int userId,
            ChangePasswordDTO dto
    ) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "User not found");
        }

        User user = userOptional.get();

        // Verify old password
        if (!passwordEncoder.matches(
                dto.getOldPassword(),
                user.getPassword()
        )) {
            return new ApiResponse<>(false, "Old password is incorrect");
        }

        // Encrypt new password
        String encodedPassword =
                passwordEncoder.encode(dto.getNewPassword());

        userRepository.updatePassword(userId, encodedPassword);

        return new ApiResponse<>(true, "Password changed successfully");
    }

    public ApiResponse<Void> deleteUser(
            int loggedInUserId,
            String loggedInRole,
            String loggedInEmail,
            int targetUserId
    ) {

        if (!isAdminUser(loggedInEmail)) {
            return new ApiResponse<>(false,
                    "Only admin can manage users");
        }

        // prevent self delete
        if (loggedInUserId == targetUserId) {
            return new ApiResponse<>(false,
                    "You cannot delete your own account");
        }

        Optional<User> targetUserOptional =
                userRepository.findById(targetUserId);

        if (targetUserOptional.isEmpty()) {
            return new ApiResponse<>(false,
                    "Target user not found");
        }

        User targetUser = targetUserOptional.get();

        // SUPERVISOR rules
        if (!targetUser.getStatus()) {
            return new ApiResponse<>(false,
                    "User already deactivated");
        }

        int updatedRows = userRepository.deleteUserById(targetUserId);

        if (updatedRows == 0) {
            return new ApiResponse<>(false,
                    "User already deactivated");
        }

        return new ApiResponse<>(true,
                "User deactivated successfully");
    }

    public ApiResponse<List<User>> getAllUsers(String loggedInEmail) {

        if (!isAdminUser(loggedInEmail)) {
            return new ApiResponse<>(false,
                    "Only admin can manage users",
                    List.of());
        }

        return new ApiResponse<>(true,
                "Users fetched successfully",
                userRepository.findAll());
    }

    private boolean isAdminUser(String email) {
        return email != null && ADMIN_EMAIL.equalsIgnoreCase(email.trim());
    }
}
