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
}
