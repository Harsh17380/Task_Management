package com.TaskManager.Taskmanager.dto;

public class SubTaskRequestDTO {

    private int taskId;
    private String title;
    private int assignedTo;
    private int tlId;

    // getters & setters

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(int assignedTo) {
        this.assignedTo = assignedTo;
    }

    public int getTlId() {
        return tlId;
    }

    public void setTlId(int tlId) {
        this.tlId = tlId;
    }
}
