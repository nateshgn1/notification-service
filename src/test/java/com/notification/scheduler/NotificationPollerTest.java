package com.notification.scheduler;

import com.notification.dispatcher.NotificationDispatcherService;
import com.notification.model.entity.Notification;
import com.notification.model.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPollerTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationDispatcherService dispatcher;

    private NotificationPoller poller;

    @BeforeEach
    void setup() {
        poller = new NotificationPoller(repository, dispatcher, 10);
    }

    @Test
    void shouldDispatchReadyNotifications() {

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.CREATED);

        when(repository.findByStatusAndScheduledAtBefore(
                any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(notification)));

        when(repository.findByStatusAndNextRetryAtBefore(
                any(), any(), any()))
                .thenReturn(Page.empty());

        poller.pollNotifications();

        verify(dispatcher).dispatch(notification);
    }

    @Test
    void shouldReturnEarlyWhenNoNotificationsFound() {

        when(repository.findByStatusAndScheduledAtBefore(
                any(), any(), any()))
                .thenReturn(Page.empty());

        when(repository.findByStatusAndNextRetryAtBefore(
                any(), any(), any()))
                .thenReturn(Page.empty());

        poller.pollNotifications();

        verify(dispatcher, never()).dispatch(any());
    }
}
