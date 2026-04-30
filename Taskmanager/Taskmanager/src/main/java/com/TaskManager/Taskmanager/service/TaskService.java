package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.repository.TaskRepository;
import com.TaskManager.Taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.TaskManager.Taskmanager.model.User;
import com.TaskManager.Taskmanager.model.Task;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public String createTask(Task task) {

        // Validate role (VERY IMPORTANT)
        List<User> tlList = userRepository.findByRole("TL");

        boolean isValidTL = tlList.stream()
                .anyMatch(user -> user.getId() == task.getAssignedTo());

        if (!isValidTL) {
            return "Invalid TL ID";
        }

        task.setStatus("CREATED");

        taskRepository.createTask(task);
        return "Task created and assigned to TL";
    }

    public List<Task> getTasksForTL(int tlId) {
        return taskRepository.findTasksByTL(tlId);
    }
}
