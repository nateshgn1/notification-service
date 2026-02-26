package com.notification.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BulkNotificationResponse {

    private int totalRequested;
    private int totalAccepted;
    private int totalRejected;
    private List<BulkItemResult> accepted;
    private List<BulkItemResult> rejected;
}