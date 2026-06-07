package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.*;
import com.TaskManager.Taskmanager.model.Company;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.repository.CompanyRepository;
import com.TaskManager.Taskmanager.repository.UserRepository;
import com.TaskManager.Taskmanager.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final List<String> COMPANY_ROLES =
            List.of("SUPERVISOR", "MANAGER", "TL", "DEVELOPER");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse<Void> createUser(UserRequestDTO dto, String loggedInRole, Integer loggedInCompanyId) {

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return new ApiResponse<>(false, "Name is required");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return new ApiResponse<>(false, "Email is required");
        }

        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            return new ApiResponse<>(false, "Password is required");
        }

        if (dto.getRole() == null || dto.getRole().trim().isEmpty()) {
            return new ApiResponse<>(false, "Role is required");
        }

        if (userRepository.findByEmail(dto.getEmail().trim()).isPresent()) {
            return new ApiResponse<>(false, "Email is already registered");
        }

        String role = dto.getRole().trim().toUpperCase();
        Integer companyId;

        if ("SUPER_ADMIN".equals(loggedInRole)) {
            if (!"COMPANY_ADMIN".equals(role)) {
                return new ApiResponse<>(false, "Platform admin can only create company administrators");
            }

            if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
                return new ApiResponse<>(false, "Company name is required");
            }

            String companyName = dto.getCompanyName().trim();
            if (companyRepository.existsByName(companyName)) {
                return new ApiResponse<>(false, "Company name is already registered");
            }
            companyId = companyRepository.create(companyName);
        } else if ("COMPANY_ADMIN".equals(loggedInRole)) {
            if (loggedInCompanyId == null || loggedInCompanyId <= 0) {
                return new ApiResponse<>(false, "Company account is not configured");
            }
            if (!COMPANY_ROLES.contains(role)) {
                return new ApiResponse<>(false, "Company admin can create Supervisor, Manager, TL, or Developer users");
            }
            companyId = loggedInCompanyId;
        } else {
            return new ApiResponse<>(false, "You do not have permission to create users");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setStatus(true);
        user.setCompanyId(companyId);

        userRepository.save(user);

        return new ApiResponse<>(true,
                "SUPER_ADMIN".equals(loggedInRole)
                        ? "Company and company administrator created successfully"
                        : "User created successfully");
    }

    public List<User> getUsersByRole(String role, String loggedInRole, Integer companyId) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        if ("SUPER_ADMIN".equals(loggedInRole)) {
            return userRepository.findByRole(normalizedRole);
        }
        if (companyId == null || companyId <= 0) {
            return List.of();
        }
        if ("COMPANY_ADMIN".equals(loggedInRole)) {
            return userRepository.findByRoleAndCompany(normalizedRole, companyId);
        }
        if ("SUPERVISOR".equals(loggedInRole) && "TL".equals(normalizedRole)) {
            return userRepository.findByRoleAndCompany(normalizedRole, companyId);
        }
        if ("TL".equals(loggedInRole) && "DEVELOPER".equals(normalizedRole)) {
            return userRepository.findByRoleAndCompany(normalizedRole, companyId);
        }
        return List.of();
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

        if (foundUser.getCompanyId() != null) {
            Optional<Company> company = companyRepository.findById(foundUser.getCompanyId());
            if (company.isEmpty() || !company.get().getStatus()) {
                return new ApiResponse<>(false, "Company account is inactive");
            }
        }

        String token = jwtUtil.generateToken(
                foundUser.getEmail(),
                foundUser.getId(),
                foundUser.getRole(),
                foundUser.getCompanyId()
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
            Integer loggedInCompanyId,
            int targetUserId
    ) {

        if (!"SUPER_ADMIN".equals(loggedInRole) && !"COMPANY_ADMIN".equals(loggedInRole)) {
            return new ApiResponse<>(false,
                    "Only platform or company administrators can manage users");
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

        if ("SUPER_ADMIN".equals(targetUser.getRole())) {
            return new ApiResponse<>(false, "Platform administrator cannot be deactivated");
        }

        if ("COMPANY_ADMIN".equals(loggedInRole)) {
            if (loggedInCompanyId == null || !loggedInCompanyId.equals(targetUser.getCompanyId())) {
                return new ApiResponse<>(false, "You can only manage users in your company");
            }
            if ("COMPANY_ADMIN".equals(targetUser.getRole())) {
                return new ApiResponse<>(false, "Company administrator cannot deactivate another company administrator");
            }
        }

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

    public ApiResponse<List<User>> getAllUsers(String loggedInRole, Integer companyId) {

        if ("SUPER_ADMIN".equals(loggedInRole)) {
            return new ApiResponse<>(true,
                    "Users fetched successfully",
                    userRepository.findAll());
        }

        if (!"COMPANY_ADMIN".equals(loggedInRole) || companyId == null) {
            return new ApiResponse<>(false,
                    "Only platform or company administrators can manage users",
                    List.of());
        }

        return new ApiResponse<>(true,
                "Users fetched successfully",
                userRepository.findAllByCompany(companyId));
    }
}
