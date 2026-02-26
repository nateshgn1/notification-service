package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.entity.Notification;
import com.notification.model.entity.UserChannelEndpoint;
import com.notification.model.enums.ChannelType;
import com.notification.provider.PushProvider;
import com.notification.repository.UserChannelEndpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationChannelTest {

    @Mock
    private UserChannelEndpointRepository endpointRepository;

    @Mock
    private PushProvider pushProvider;

    @InjectMocks
    private PushNotificationChannel pushChannel;


    @Test
    void shouldReturnSupportedChannel() {
        assertThat(pushChannel.getSupportedChannel())
                .isEqualTo(ChannelType.PUSH);
    }

    @Test
    void shouldSendPushNotification() {

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannelType(ChannelType.PUSH);
        notification.setPayload("test");

        UserChannelEndpoint endpoint = new UserChannelEndpoint();
        endpoint.setEndpointValue("device-token-123");

        when(endpointRepository.findByUserIdAndChannelType(1L, ChannelType.PUSH))
                .thenReturn(Optional.of(endpoint));

        pushChannel.send(notification);

        verify(pushProvider).send("device-token-123", "test");
    }

    @Test
    void shouldThrowIfPushEndpointMissing() {

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannelType(ChannelType.PUSH);

        when(endpointRepository.findByUserIdAndChannelType(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pushChannel.send(notification))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldValidateValidPushToken() {
        pushChannel.validateEndpoint("valid-token-12345");
    }

    @Test
    void shouldThrowIfPushTokenInvalid() {
        assertThatThrownBy(() ->
                pushChannel.validateEndpoint("short"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid push token");
    }
}
