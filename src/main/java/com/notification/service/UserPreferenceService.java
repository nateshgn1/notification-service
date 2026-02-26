package com.notification.service;

import com.notification.model.enums.ChannelType;

public interface UserPreferenceService {

    void validateChannelEnabled(Long userId, ChannelType channelType);
}
