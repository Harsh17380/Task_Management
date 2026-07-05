package com.TaskManager.Taskmanager.service;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.model.Notification;
import com.TaskManager.Taskmanager.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // ── Public helpers called by other services ──────────────────

    /**
     * Push a notification to a single user.
     * Runs asynchronously so it never blocks the main request.
     */
    @Async
    public void push(int userId, Integer companyId, String type, String message, Integer referenceId) {
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setCompanyId(companyId == null ? 0 : companyId);
            n.setType(type);
            n.setMessage(message);
            n.setReferenceId(referenceId);
            notificationRepository.create(n);
        } catch (Exception e) {
            // Non-critical — log and swallow so main flow is never broken
            System.err.println("[NotificationService] Failed to push notification: " + e.getMessage());
        }
    }

    // ── API methods used by NotificationController ───────────────

    public ApiResponse<List<Notification>> getNotifications(int userId) {
        List<Notification> list = notificationRepository.getUserNotifications(userId);
        return new ApiResponse<>(true, "Notifications fetched", list);
    }

    public ApiResponse<Integer> getUnreadCount(int userId) {
        int count = notificationRepository.getUnreadCount(userId);
        return new ApiResponse<>(true, "Unread count fetched", count);
    }

    public ApiResponse<Void> markAsRead(int notificationId, int userId) {
        int updatedRows = notificationRepository.markAsRead(notificationId, userId);
        if (updatedRows == 0) {
            return new ApiResponse<>(false, "Notification not found");
        }
        return new ApiResponse<>(true, "Marked as read");
    }

    public ApiResponse<Void> markAllAsRead(int userId) {
        notificationRepository.markAllAsRead(userId);
        return new ApiResponse<>(true, "All notifications marked as read");
    }
}
