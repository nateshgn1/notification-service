package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.entity.Notification;
import com.notification.model.entity.UserChannelEndpoint;
import com.notification.model.enums.ChannelType;
import com.notification.provider.SmsProvider;
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
class SmsNotificationChannelTest {

    @Mock
    private UserChannelEndpointRepository endpointRepository;

    @Mock
    private SmsProvider smsProvider;

    @InjectMocks
    private SmsNotificationChannel smsChannel;


    @Test
    void shouldReturnSupportedChannel() {
        assertThat(smsChannel.getSupportedChannel())
                .isEqualTo(ChannelType.SMS);
    }

    @Test
    void shouldSendSmsNotification() {

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannelType(ChannelType.SMS);
        notification.setPayload("test");

        UserChannelEndpoint endpoint = new UserChannelEndpoint();
        endpoint.setEndpointValue("9876543210");

        when(endpointRepository.findByUserIdAndChannelType(1L, ChannelType.SMS))
                .thenReturn(Optional.of(endpoint));

        smsChannel.send(notification);

        verify(smsProvider).send("9876543210", "test");
    }

    @Test
    void shouldThrowIfSmsEndpointMissing() {

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannelType(ChannelType.SMS);

        when(endpointRepository.findByUserIdAndChannelType(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                smsChannel.send(notification))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldValidateValidPhoneNumber() {
        smsChannel.validateEndpoint("9876543210");
    }

    @Test
    void shouldThrowIfPhoneInvalid() {
        assertThatThrownBy(() ->
                smsChannel.validateEndpoint("123"))
                .isInstanceOf(BadRequestException.class);
    }
}