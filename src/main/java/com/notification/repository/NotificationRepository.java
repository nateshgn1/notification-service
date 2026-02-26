package com.notification.repository;

import com.notification.model.entity.Notification;
import com.notification.model.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByStatusAndScheduledAtBefore(
            NotificationStatus status,
            LocalDateTime time,
            Pageable pageable
    );

    Page<Notification> findByStatusAndNextRetryAtBefore(
            NotificationStatus status,
            LocalDateTime time,
            Pageable pageable
    );

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndStatus(
            Long userId,
            NotificationStatus status,
            Pageable pageable
    );
}
