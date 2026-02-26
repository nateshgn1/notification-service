package com.notification.model.dto.response;

import com.notification.model.enums.NotificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private Long notificationId;
    private NotificationStatus status;
}