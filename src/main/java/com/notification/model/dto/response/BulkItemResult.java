package com.notification.model.dto.response;

import com.notification.model.enums.ChannelType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BulkItemResult {

    private Long userId;
    private ChannelType channelType;
    private Long notificationId; // present only if accepted
    private String error;  // present only if rejected
}