package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.LoginRequestDTO;
import com.TaskManager.Taskmanager.dto.LoginResponseDTO;
import com.TaskManager.Taskmanager.dto.UserRequestDTO;
import com.TaskManager.Taskmanager.dto.UserResponseDTO;
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

}
