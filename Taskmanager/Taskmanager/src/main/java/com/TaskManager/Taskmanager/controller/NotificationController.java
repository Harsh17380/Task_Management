package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.dto.ApiResponse;
import com.TaskManager.Taskmanager.model.Notification;
import com.TaskManager.Taskmanager.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /** GET /notifications — returns last 50 notifications for the logged-in user */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Not authenticated", List.of()));
        }
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    /** GET /notifications/unread-count */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Not authenticated", 0));
        }
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    /** PUT /notifications/{id}/read — mark a single notification as read */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable int id,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Not authenticated"));
        }
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    /** PUT /notifications/read-all — mark all as read for logged-in user */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Not authenticated"));
        }
        return ResponseEntity.ok(notificationService.markAllAsRead(userId));
    }
}
