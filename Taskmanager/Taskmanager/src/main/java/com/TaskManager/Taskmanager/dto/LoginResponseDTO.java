package com.TaskManager.Taskmanager.dto;

import com.TaskManager.Taskmanager.model.User;

public class LoginResponseDTO {

    private int id;
    private String name;
    private String email;
    private String role;
    private String token;  // JWT token

    public LoginResponseDTO(User user, String token) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.token = token;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
