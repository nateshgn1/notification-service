package com.notification.service;

import com.notification.model.dto.request.AddChannelEndpointRequest;
import com.notification.model.dto.request.CreateUserRequest;
import com.notification.model.dto.response.ChannelEndpointResponse;
import com.notification.model.dto.response.UserResponse;
import com.notification.model.enums.ChannelType;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    ChannelEndpointResponse addChannelEndpoint(Long userId, AddChannelEndpointRequest request);

    void updateUserPreference(Long userId, ChannelType channelType, Boolean enabled);
}
