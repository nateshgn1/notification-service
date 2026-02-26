package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.entity.Notification;
import com.notification.model.entity.UserChannelEndpoint;
import com.notification.model.enums.ChannelType;
import com.notification.provider.EmailProvider;
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
public class EmailNotificationChannelTest {

    @Mock
    private UserChannelEndpointRepository endpointRepository;

    @Mock
    private EmailProvider emailProvider;

    @InjectMocks
    private EmailNotificationChannel emailChannel;

    @Test
    void shouldReturnSupportedChannel() {
        assertThat(emailChannel.getSupportedChannel())
                .isEqualTo(ChannelType.EMAIL);
    }

    @Test
    void shouldSendEmailNotification() {

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannelType(ChannelType.EMAIL);
        notification.setPayload("test");

        UserChannelEndpoint endpoint = new UserChannelEndpoint();
        endpoint.setEndpointValue("test@example.com");

        when(endpointRepository.findByUserIdAndChannelType(1L, ChannelType.EMAIL))
                .thenReturn(Optional.of(endpoint));

        emailChannel.send(notification);

        verify(emailProvider).send("test@example.com", "test");
    }


    @Test
    void shouldThrowIfEmailEndpointMissing() {

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannelType(ChannelType.EMAIL);

        when(endpointRepository.findByUserIdAndChannelType(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                emailChannel.send(notification))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldValidateValidEmail() {
        emailChannel.validateEndpoint("test@example.com");
    }

    @Test
    void shouldThrowIfEmailInvalid() {
        assertThatThrownBy(() ->
                emailChannel.validateEndpoint("invalid-email"))
                .isInstanceOf(BadRequestException.class);
    }
}
