package com.notification.model.dto.request;

import com.notification.model.enums.ChannelType;
import com.notification.model.enums.NotificationPriority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateNotificationRequest {

    @NotNull
    private Long userId;

    @NotNull
    private ChannelType channelType;

    @NotBlank
    private String payload;

    @NotNull
    private NotificationPriority priority;

    private LocalDateTime scheduledAt;

    @Min(value = 1, message = "Recurrence interval must be positive")
    private Long recurrenceIntervalMinutes;
}
