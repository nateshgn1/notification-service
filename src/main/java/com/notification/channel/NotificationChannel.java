package com.notification.channel;

import com.notification.model.entity.Notification;
import com.notification.model.enums.ChannelType;

public interface NotificationChannel {

    ChannelType getSupportedChannel();

    void send(Notification notification);

    void validateEndpoint(String endpointValue);
}


