package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.entity.Notification;
import com.notification.model.enums.ChannelType;
import com.notification.provider.SmsProvider;
import com.notification.repository.UserChannelEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsNotificationChannel implements NotificationChannel {

    private final SmsProvider smsProvider;
    private final UserChannelEndpointRepository endpointRepository;

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.SMS;
    }

    @Override
    public void send(Notification notification) {
        String phoneNumber = endpointRepository
                .findByUserIdAndChannelType(notification.getUserId(), ChannelType.SMS)
                .orElseThrow(() -> new IllegalStateException("SMS endpoint not found for userId=" + notification.getUserId()))
                .getEndpointValue();

        smsProvider.send(phoneNumber, notification.getPayload());
    }

    @Override
    public void validateEndpoint(String endpointValue) {

        if (!endpointValue.matches("^[0-9]{10,15}$")) {
            throw new BadRequestException("Invalid phone number format");
        }
    }
}
