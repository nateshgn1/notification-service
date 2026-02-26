package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.entity.Notification;
import com.notification.model.enums.ChannelType;
import com.notification.provider.EmailProvider;
import com.notification.repository.UserChannelEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {

    private final EmailProvider emailProvider;
    private final UserChannelEndpointRepository endpointRepository;

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.EMAIL;
    }

    @Override
    public void send(Notification notification) {

        String email = endpointRepository
                .findByUserIdAndChannelType(notification.getUserId(), ChannelType.EMAIL)
                .orElseThrow(() -> new IllegalStateException("Email endpoint not found for userId=" + notification.getUserId()))
                .getEndpointValue();

        emailProvider.send(email, notification.getPayload());
    }

    @Override
    public void validateEndpoint(String endpointValue) {

        if (!endpointValue.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BadRequestException("Invalid email format");
        }
    }
}

