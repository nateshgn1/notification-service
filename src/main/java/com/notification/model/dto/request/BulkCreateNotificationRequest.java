package com.notification.model.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkCreateNotificationRequest {

    @NotEmpty(message = "list cannot be empty")
    @Valid
    private List<CreateNotificationRequest> notifications;
}