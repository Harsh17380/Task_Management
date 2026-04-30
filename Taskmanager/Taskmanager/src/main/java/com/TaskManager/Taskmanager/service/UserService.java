package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.dto.UserRequestDTO;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ApiResponse createUser(UserRequestDTO dto) {

        if (dto.getRole() == null) {
            return new ApiResponse(false, "Role is required");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());

        userRepository.save(user);

        return new ApiResponse(true, "User created successfully");
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
}
