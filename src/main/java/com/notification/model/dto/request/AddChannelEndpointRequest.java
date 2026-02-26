package com.notification.model.dto.request;

import com.notification.model.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddChannelEndpointRequest {

    @NotNull
    private ChannelType channelType;

    @NotBlank
    private String endpointValue;
}
