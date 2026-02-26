package com.notification.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePreferenceRequest {

    @NotNull
    private Boolean enabled;
}