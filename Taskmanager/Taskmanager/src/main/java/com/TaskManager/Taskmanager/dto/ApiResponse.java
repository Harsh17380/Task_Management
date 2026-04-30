package com.TaskManager.Taskmanager.dto;

public class ApiResponse {

    private boolean success;
    private String message;

    // constructor
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // getters & setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
