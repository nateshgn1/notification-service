package com.notification.dispatcher;

import com.notification.channel.NotificationChannel;
import com.notification.channel.NotificationChannelFactory;
import com.notification.model.entity.Notification;
import com.notification.model.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationDispatcherServiceImpl implements NotificationDispatcherService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatcherServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationChannelFactory channelFactory;

    @Override
    @Transactional
    public void dispatch(Notification notification) {

        logger.info("Dispatching notification with id={} channel={}",
                notification.getId(),
                notification.getChannelType());

        try {

            // Move to PROCESSING
            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);

            NotificationChannel channel = channelFactory.getChannel(notification.getChannelType());

            channel.send(notification);

            handleSuccess(notification);
            logger.info("Notification sent successfully id={} channel={}",
                    notification.getId(),
                    notification.getChannelType());

        } catch (Exception ex) {

            logger.error("Error sending notification id={}",
                    notification.getId(),
                    ex);

            handleFailure(notification);
        }
    }

    private void handleSuccess(Notification notification) {

        if (notification.getRecurrenceIntervalMinutes() != null) {

            logger.info("Recurring notification detected. Rescheduling id={}",
                    notification.getId());

            notification.setStatus(NotificationStatus.CREATED);

            notification.setScheduledAt(
                    LocalDateTime.now()
                            .plusMinutes(notification.getRecurrenceIntervalMinutes())
            );

            notification.setRetryCount(0);
            notification.setNextRetryAt(null);

        } else {

            notification.setStatus(NotificationStatus.SENT);
        }
        notificationRepository.save(notification);
    }

    private void handleFailure(Notification notification) {

        int nextRetryCount = notification.getRetryCount() + 1;
        notification.setRetryCount(nextRetryCount);

        logger.warn("Failure detected for notification with id={} attempt={} maxRetries={}",
                notification.getId(),
                nextRetryCount,
                notification.getMaxRetries());

        if (nextRetryCount > notification.getMaxRetries()) {

            notification.setStatus(NotificationStatus.DEAD_LETTER);
            notification.setNextRetryAt(null);

            logger.warn("Notification id={} exhausted retries. Moved to DEAD_LETTER",
                    notification.getId());

        } else {

            logger.info("Updating status id={} from {} to FAILED",
                    notification.getId(),
                    notification.getStatus());
            notification.setStatus(NotificationStatus.FAILED);

            //exponential backoff
            // Retry 1 → 1 min
            // Retry 2 → 2 min
            // Retry 3 → 4 min
            long delayMinutes = (long) Math.pow(2, nextRetryCount - 1);

            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));

            logger.info("Retry scheduled for notification id={} at {}",
                    notification.getId(),
                    notification.getNextRetryAt());
        }
        notificationRepository.save(notification);
    }
}
