package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.enums.ChannelType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationChannelFactoryTest {

    @Test
    void shouldReturnCorrectChannel() {

        NotificationChannel emailChannel = mock(NotificationChannel.class);
        when(emailChannel.getSupportedChannel()).thenReturn(ChannelType.EMAIL);

        NotificationChannelFactory factory =
                new NotificationChannelFactory(List.of(emailChannel));

        NotificationChannel result = factory.getChannel(ChannelType.EMAIL);

        assertThat(result).isEqualTo(emailChannel);
    }

    @Test
    void shouldThrowIfUnsupportedChannel() {

        NotificationChannelFactory factory =
                new NotificationChannelFactory(List.of());

        assertThatThrownBy(() ->
                factory.getChannel(ChannelType.EMAIL))
                .isInstanceOf(BadRequestException.class);
    }
}
