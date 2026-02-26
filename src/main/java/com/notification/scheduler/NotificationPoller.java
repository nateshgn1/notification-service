package com.notification.scheduler;

import com.notification.model.entity.Notification;
import com.notification.model.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import com.notification.dispatcher.NotificationDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationPoller {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPoller.class);

    private final int batchSize;
    private final NotificationRepository notificationRepository;
    private final NotificationDispatcherService dispatcherService;

    public NotificationPoller(
            NotificationRepository notificationRepository,
            NotificationDispatcherService dispatcherService,
            @Value("${notification.polling.batch-size}") int batchSize
    ) {
        this.notificationRepository = notificationRepository;
        this.dispatcherService = dispatcherService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${notification.polling.delay-ms}")
    public void pollNotifications() {

        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(
                0,
                batchSize,
                Sort.by(
                        Sort.Order.desc("priorityWeight"),
                        Sort.Order.asc("createdAt")
                )
        );

        List<Notification> readyNotifications =
                notificationRepository
                        .findByStatusAndScheduledAtBefore(
                                NotificationStatus.CREATED,
                                now,
                                pageable
                        )
                        .getContent();

        List<Notification> retryNotifications =
                notificationRepository
                        .findByStatusAndNextRetryAtBefore(
                                NotificationStatus.FAILED,
                                now,
                                pageable
                        )
                        .getContent();

        int total = readyNotifications.size() + retryNotifications.size();

        if (total == 0) {
            return;
        }

        logger.info("Processing {} notifications (new={}, retry={})",
                total,
                readyNotifications.size(),
                retryNotifications.size());

        readyNotifications.forEach(dispatcherService::dispatch);
        retryNotifications.forEach(dispatcherService::dispatch);
    }
}