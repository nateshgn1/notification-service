package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.entity.Notification;
import com.notification.model.enums.ChannelType;
import com.notification.provider.PushProvider;
import com.notification.repository.UserChannelEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushNotificationChannel implements NotificationChannel {

    private final PushProvider pushProvider;
    private final UserChannelEndpointRepository endpointRepository;

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.PUSH;
    }

    @Override
    public void send(Notification notification) {
        String deviceToken = endpointRepository
                .findByUserIdAndChannelType(notification.getUserId(), ChannelType.PUSH)
                .orElseThrow(() -> new IllegalStateException("Push endpoint not found for userId=" + notification.getUserId()))
                .getEndpointValue();

        pushProvider.send(deviceToken, notification.getPayload());
    }

    @Override
    public void validateEndpoint(String endpointValue) {

        if (endpointValue.length() < 10) {
            throw new BadRequestException("Invalid push token");
        }
    }
}
