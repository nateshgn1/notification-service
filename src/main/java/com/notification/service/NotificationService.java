package com.notification.service;

import com.notification.model.dto.request.CreateNotificationRequest;
import com.notification.model.dto.response.BulkNotificationResponse;
import com.notification.model.dto.response.NotificationResponse;
import com.notification.model.dto.response.PagedResponse;
import com.notification.model.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    BulkNotificationResponse createBulkNotifications(List<CreateNotificationRequest> requests);

    NotificationResponse getNotificationById(Long id);

    PagedResponse<NotificationResponse> getNotificationsByUser(Long userId, NotificationStatus status, Pageable pageable);
}
