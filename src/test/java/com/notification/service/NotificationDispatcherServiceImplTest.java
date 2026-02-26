package com.notification.service;

import com.notification.channel.NotificationChannel;
import com.notification.channel.NotificationChannelFactory;
import com.notification.dispatcher.NotificationDispatcherServiceImpl;
import com.notification.model.entity.Notification;
import com.notification.model.enums.ChannelType;
import com.notification.model.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class NotificationDispatcherServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationChannelFactory channelFactory;

    @Mock
    private NotificationChannel channel;

    @InjectMocks
    private NotificationDispatcherServiceImpl dispatcher;

    @Test
    void shouldMarkSentWhenChannelSucceeds() {

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setChannelType(ChannelType.EMAIL);
        notification.setStatus(NotificationStatus.CREATED);

        when(channelFactory.getChannel(ChannelType.EMAIL)).thenReturn(channel);

        dispatcher.dispatch(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(notificationRepository, atLeastOnce()).save(notification);
    }

    @Test
    void shouldRetryOnFailure() {

        Notification notification = new Notification();
        notification.setId(2L);
        notification.setChannelType(ChannelType.EMAIL);
        notification.setStatus(NotificationStatus.CREATED);
        notification.setMaxRetries(3);

        when(channelFactory.getChannel(ChannelType.EMAIL)).thenReturn(channel);
        doThrow(new RuntimeException()).when(channel).send(notification);

        dispatcher.dispatch(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getRetryCount()).isEqualTo(1);
    }

    @Test
    void shouldMoveToDeadLetterAfterMaxRetries() {

        Notification notification = new Notification();
        notification.setId(3L);
        notification.setChannelType(ChannelType.EMAIL);
        notification.setStatus(NotificationStatus.CREATED);
        notification.setRetryCount(3);
        notification.setMaxRetries(3);

        when(channelFactory.getChannel(ChannelType.EMAIL)).thenReturn(channel);
        doThrow(new RuntimeException()).when(channel).send(notification);

        dispatcher.dispatch(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DEAD_LETTER);
    }

    @Test
    void shouldSetNextRetryTimestampOnFailure() {

        Notification notification = new Notification();
        notification.setId(4L);
        notification.setChannelType(ChannelType.EMAIL);
        notification.setMaxRetries(5);

        when(channelFactory.getChannel(ChannelType.EMAIL)).thenReturn(channel);
        doThrow(new RuntimeException()).when(channel).send(notification);

        dispatcher.dispatch(notification);

        assertThat(notification.getNextRetryAt()).isNotNull();
    }

    @Test
    void shouldRescheduleIfRecurring() {

        Notification notification = new Notification();
        notification.setId(5L);
        notification.setChannelType(ChannelType.EMAIL);
        notification.setStatus(NotificationStatus.CREATED);
        notification.setRecurrenceIntervalMinutes(5L);

        when(channelFactory.getChannel(ChannelType.EMAIL)).thenReturn(channel);

        dispatcher.dispatch(notification);

        assertThat(notification.getStatus())
                .isEqualTo(NotificationStatus.CREATED);

        assertThat(notification.getScheduledAt())
                .isNotNull();

        assertThat(notification.getRetryCount())
                .isEqualTo(0);

        verify(notificationRepository, atLeastOnce())
                .save(notification);
    }
}
