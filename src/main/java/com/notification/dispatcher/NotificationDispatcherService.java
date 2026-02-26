package com.notification.dispatcher;

import com.notification.model.entity.Notification;

public interface NotificationDispatcherService {
    void dispatch(Notification notification);
}
