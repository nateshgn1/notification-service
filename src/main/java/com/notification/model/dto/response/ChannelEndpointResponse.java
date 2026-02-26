package com.notification.model.dto.response;

import com.notification.model.enums.ChannelType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelEndpointResponse {

    private Long userId;
    private ChannelType channelType;
    private String endpointValue;
}