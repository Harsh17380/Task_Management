package com.TaskManager.Taskmanager.repository;

import com.TaskManager.Taskmanager.model.Notification;
import java.util.List;

public interface NotificationRepository {

    void create(Notification notification);

    List<Notification> getUserNotifications(int userId);

    int getUnreadCount(int userId);

    int markAsRead(int notificationId, int userId);

    void markAllAsRead(int userId);
}
