package com.notification.controller;

import com.notification.exception.BadRequestException;
import com.notification.model.dto.request.BulkCreateNotificationRequest;
import com.notification.model.dto.request.CreateNotificationRequest;
import com.notification.model.dto.response.BulkNotificationResponse;
import com.notification.model.dto.response.NotificationResponse;
import com.notification.model.dto.response.PagedResponse;
import com.notification.model.enums.NotificationStatus;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request
    ) {

        logger.info("Received notification creation request for userId={}", request.getUserId());

        NotificationResponse response =
                notificationService.createNotification(request);

        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkNotificationResponse> createBulkNotifications(
            @RequestBody @Valid BulkCreateNotificationRequest request
    ) {
        BulkNotificationResponse response =
                notificationService.createBulkNotifications(
                        request.getNotifications()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedResponse<NotificationResponse>> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Invalid page or size parameter");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"))
        );

        return ResponseEntity.ok(
                notificationService.getNotificationsByUser(
                        userId,
                        status,
                        pageable
                )
        );
    }
}
