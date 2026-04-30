package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.controller.SubTask;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.repository.SubTaskRepository;
import com.TaskManager.Taskmanager.repository.TaskRepository;
import com.TaskManager.Taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubTaskService {

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    public String createSubTask(SubTask subTask) {

        // Validate Developer
        List<User> devs = userRepository.findByRole("DEVELOPER");

        boolean isValidDev = devs.stream()
                .anyMatch(user -> user.getId() == subTask.getAssignedTo());

        if (!isValidDev) {
            return "Invalid Developer ID";
        }

        subTask.setStatus("PENDING");

        subTaskRepository.createSubTask(subTask);
        return "Subtask created and assigned to Developer";
    }

    public List<SubTask> getSubTasksForDeveloper(int devId) {
        return subTaskRepository.findByDeveloper(devId);
    }

    public String updateStatus(int subTaskId, String status) {

        subTaskRepository.updateStatus(subTaskId, status);

        // Get taskId of this subtask
        // (you need a method for this if not already present)

        int taskId = subTaskRepository.findTaskIdBySubTaskId(subTaskId); // we'll define this

        int remaining = subTaskRepository.countIncompleteSubTasks(taskId);

        if (remaining == 0) {
            taskRepository.updateTaskStatus(taskId, "COMPLETED");
        } else {
            taskRepository.updateTaskStatus(taskId, "IN_PROGRESS");
        }

        return "Status updated";
    }
}
