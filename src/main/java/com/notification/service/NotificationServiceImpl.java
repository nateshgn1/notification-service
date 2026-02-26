package com.notification.service;

import com.notification.config.RetryProperties;
import com.notification.exception.BadRequestException;
import com.notification.exception.ResourceNotFoundException;
import com.notification.model.dto.request.CreateNotificationRequest;
import com.notification.model.dto.response.BulkItemResult;
import com.notification.model.dto.response.BulkNotificationResponse;
import com.notification.model.dto.response.NotificationResponse;
import com.notification.model.dto.response.PagedResponse;
import com.notification.model.entity.Notification;
import com.notification.model.enums.ChannelType;
import com.notification.model.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import com.notification.repository.UserChannelEndpointRepository;
import com.notification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserPreferenceService userPreferenceService;
    private final UserRepository userRepository;
    private final UserChannelEndpointRepository endpointRepository;
    private final RetryProperties retryProperties;


    @Value("${notification.bulk.max-size}")
    private int maxBulkSize;

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {

        logger.info("Creating notification for userId={}, channel={}",
                request.getUserId(),
                request.getChannelType());

        // 1. Validate preference
        validateUserAndChannel(request.getUserId(), request.getChannelType());

        // 2. Create entity
        Notification notification = buildNotificationEntity(request);

        // 3. Persist
        Notification saved = notificationRepository.save(notification);

        logger.info("Notification created successfully. notificationId={}", saved.getId());

        return mapToResponse(saved);
    }

    private Notification buildNotificationEntity(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setChannelType(request.getChannelType());
        notification.setPayload(request.getPayload());
        notification.setPriority(request.getPriority());
        notification.setPriorityWeight(request.getPriority().getWeight());
        notification.setMaxRetries(
                retryProperties.getMaxRetries(request.getChannelType())
        );
        notification.setRecurrenceIntervalMinutes(
                request.getRecurrenceIntervalMinutes()
        );
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = request.getScheduledAt();
        if (scheduledTime == null || scheduledTime.isBefore(now)) {
            scheduledTime = now;
        }
        notification.setScheduledAt(scheduledTime);
        notification.setStatus(NotificationStatus.CREATED);
        return notification;
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .status(notification.getStatus())
                .build();
    }

    @Override
    @Transactional
    public BulkNotificationResponse createBulkNotifications(
            List<CreateNotificationRequest> requests
    ) {

        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Bulk request cannot be empty");
        }

        if (requests.size() > maxBulkSize) {
            throw new BadRequestException(
                    "Bulk request exceeds maximum allowed size of " + maxBulkSize
            );
        }

        logger.info("Processing bulk notification request size={}", requests.size());

        List<Notification> validNotifications = new ArrayList<>();
        List<BulkItemResult> rejectedResults = new ArrayList<>();

        // Validation phase
        for (CreateNotificationRequest request : requests) {

            try {
                validateUserAndChannel(
                        request.getUserId(),
                        request.getChannelType()
                );

                Notification notification = buildNotificationEntity(request);
                validNotifications.add(notification);

            } catch (RuntimeException ex) {

                rejectedResults.add(
                        BulkItemResult.builder()
                                .userId(request.getUserId())
                                .channelType(request.getChannelType())
                                .error(ex.getMessage())
                                .build()
                );
            }
        }

        // Persist all valid notifications in one batch
        List<Notification> savedNotifications = validNotifications.isEmpty()
                ? List.of() : notificationRepository.saveAll(validNotifications);

        // Build accepted results from saved entities
        List<BulkItemResult> acceptedResults = savedNotifications.stream()
                .map(saved -> BulkItemResult.builder()
                        .userId(saved.getUserId())
                        .channelType(saved.getChannelType())
                        .notificationId(saved.getId())
                        .build()
                )
                .toList();

        logger.info("Bulk ingestion completed accepted={} rejected={}",
                acceptedResults.size(), rejectedResults.size());

        return BulkNotificationResponse.builder()
                .totalRequested(requests.size())
                .totalAccepted(acceptedResults.size())
                .totalRejected(rejectedResults.size())
                .accepted(acceptedResults)
                .rejected(rejectedResults)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found"));

        return mapToResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotificationsByUser(
            Long userId,
            NotificationStatus status,
            Pageable pageable
    ) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Page<Notification> pageResult;

        if (status != null) {
            pageResult = notificationRepository
                    .findByUserIdAndStatus(userId, status, pageable);
        } else {
            pageResult = notificationRepository
                    .findByUserId(userId, pageable);
        }

        List<NotificationResponse> content =
                pageResult.map(this::mapToResponse).getContent();

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .build();
    }

    private void validateUserAndChannel(Long userId, ChannelType channelType) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        endpointRepository.findByUserIdAndChannelType(userId, channelType)
                .orElseThrow(() ->
                        new BadRequestException("Channel endpoint not configured for user"));

        userPreferenceService.validateChannelEnabled(userId, channelType);
    }
}



