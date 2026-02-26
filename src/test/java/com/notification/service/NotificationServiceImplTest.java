package com.notification.service;

import com.notification.config.RetryProperties;
import com.notification.exception.BadRequestException;
import com.notification.exception.ResourceNotFoundException;
import com.notification.model.dto.request.CreateNotificationRequest;
import com.notification.model.dto.response.NotificationResponse;
import com.notification.model.entity.Notification;
import com.notification.model.entity.User;
import com.notification.model.entity.UserChannelEndpoint;
import com.notification.model.enums.ChannelType;
import com.notification.model.enums.NotificationPriority;
import com.notification.model.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import com.notification.repository.UserChannelEndpointRepository;
import com.notification.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserPreferenceService userPreferenceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserChannelEndpointRepository endpointRepository;

    @Mock
    private RetryProperties retryProperties;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private CreateNotificationRequest request;

    @BeforeEach
    void setup() {
        request = new CreateNotificationRequest();
        request.setUserId(1L);
        request.setChannelType(ChannelType.EMAIL);
        request.setPayload("Test");
        request.setPriority(NotificationPriority.HIGH);
        request.setScheduledAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateNotificationSuccessfully() {
        mockValidUserAndEndpoint();
        Notification saved = new Notification();
        saved.setId(10L);
        saved.setStatus(NotificationStatus.CREATED);

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(saved);

        var response = notificationService.createNotification(request);

        assertThat(response.getNotificationId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.CREATED);

        verify(userPreferenceService)
                .validateChannelEnabled(1L, ChannelType.EMAIL);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldThrowIfPreferenceValidationFails() {
        mockValidUserAndChannelEndpoint();
        doThrow(new IllegalStateException("Channel disabled"))
                .when(userPreferenceService)
                .validateChannelEnabled(any(), any());

        assertThatThrownBy(() ->
                notificationService.createNotification(request))
                .isInstanceOf(IllegalStateException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldDefaultScheduledAtIfNull() {
        mockValidUserAndEndpoint();

        request.setScheduledAt(null);

        Notification saved = new Notification();
        saved.setId(20L);
        saved.setStatus(NotificationStatus.CREATED);

        when(notificationRepository.save(any())).thenReturn(saved);

        notificationService.createNotification(request);

        verify(notificationRepository).save(argThat(notification ->
                notification.getScheduledAt() != null
        ));
    }

    @Test
    void shouldCreateBulkNotificationsSuccessfully() {
        mockValidUserAndEndpoint();

        ReflectionTestUtils.setField(notificationService, "maxBulkSize", 10);

        CreateNotificationRequest r1 = buildRequest(1L);
        CreateNotificationRequest r2 = buildRequest(2L);

        Notification n1 = new Notification();
        n1.setId(100L);
        n1.setUserId(1L);
        n1.setChannelType(ChannelType.EMAIL);

        Notification n2 = new Notification();
        n2.setId(101L);
        n2.setUserId(2L);
        n2.setChannelType(ChannelType.EMAIL);

        when(notificationRepository.saveAll(argThat((List<Notification> list) -> list.size() == 2)))
                .thenReturn(List.of(n1, n2));

        var response = notificationService.createBulkNotifications(List.of(r1, r2));

        assertThat(response.getTotalRequested()).isEqualTo(2);
        assertThat(response.getTotalAccepted()).isEqualTo(2);
        assertThat(response.getTotalRejected()).isEqualTo(0);
        assertThat(response.getAccepted()).hasSize(2);
        assertThat(response.getRejected()).isEmpty();

        verify(userPreferenceService, times(2))
                .validateChannelEnabled(any(), any());
        verify(notificationRepository).saveAll(argThat((List<Notification> list) -> list.size() == 2));
    }

    @Test
    void shouldHandlePartialBulkFailures() {
        mockValidUserAndEndpoint();

        ReflectionTestUtils.setField(notificationService, "maxBulkSize", 10);

        CreateNotificationRequest r1 = buildRequest(1L);
        CreateNotificationRequest r2 = buildRequest(2L);

        doNothing().when(userPreferenceService)
                .validateChannelEnabled(1L, ChannelType.EMAIL);

        // Only second user fails
        doThrow(new IllegalStateException("Channel disabled"))
                .when(userPreferenceService)
                .validateChannelEnabled(2L, ChannelType.EMAIL);

        Notification saved = new Notification();
        saved.setId(200L);
        saved.setUserId(1L);
        saved.setChannelType(ChannelType.EMAIL);

        when(notificationRepository.saveAll(argThat((List<Notification> list) -> list.size() == 1)))
                .thenReturn(List.of(saved));

        var response = notificationService.createBulkNotifications(List.of(r1, r2));

        assertThat(response.getTotalRequested()).isEqualTo(2);
        assertThat(response.getTotalAccepted()).isEqualTo(1);
        assertThat(response.getTotalRejected()).isEqualTo(1);

        assertThat(response.getAccepted()).hasSize(1);
        assertThat(response.getAccepted().get(0).getNotificationId()).isEqualTo(200L);

        assertThat(response.getRejected()).hasSize(1);
        assertThat(response.getRejected().get(0).getError())
                .isEqualTo("Channel disabled");

        verify(notificationRepository).saveAll(argThat((List<Notification> list) -> list.size() == 1));
    }

    @Test
    void shouldThrowIfBulkEmpty() {

        assertThatThrownBy(() ->
                notificationService.createBulkNotifications(List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Bulk request cannot be empty");

        verifyNoInteractions(notificationRepository);
    }

    @Test
    void shouldThrowIfBulkExceedsMaxSize() {

        ReflectionTestUtils.setField(notificationService, "maxBulkSize", 1);

        CreateNotificationRequest r1 = buildRequest(1L);
        CreateNotificationRequest r2 = buildRequest(2L);

        assertThatThrownBy(() ->
                notificationService.createBulkNotifications(List.of(r1, r2)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds maximum");

        verifyNoInteractions(notificationRepository);
    }

    @Test
    void shouldThrowIfNotificationNotFound() {

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.getNotificationById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found");
    }

    @Test
    void shouldGetNotificationsByUserWithStatusFilter() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setStatus(NotificationStatus.SENT);

        Page<Notification> page =
                new PageImpl<>(List.of(notification));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User()));

        when(notificationRepository.findByUserIdAndStatus(
                eq(1L),
                eq(NotificationStatus.SENT),
                any()))
                .thenReturn(page);

        var result = notificationService.getNotificationsByUser(
                1L,
                NotificationStatus.SENT,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldGetNotificationsByUserWithoutStatusFilter() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User()));

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setStatus(NotificationStatus.SENT);

        Page<Notification> page =
                new PageImpl<>(List.of(notification));

        when(notificationRepository.findByUserId(eq(1L), any()))
                .thenReturn(page);

        var result = notificationService.getNotificationsByUser(
                1L,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findByUserId(eq(1L), any());
    }

    @Test
    void shouldThrowIfUserNotFoundInValidation() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.createNotification(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldThrowIfEndpointNotConfigured() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User()));

        when(endpointRepository.findByUserIdAndChannelType(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.createNotification(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Channel endpoint not configured for user");
    }

    @Test
    void shouldReturnNotificationById() {

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setStatus(NotificationStatus.SENT);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        NotificationResponse response =
                notificationService.getNotificationById(1L);

        assertThat(response.getNotificationId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void shouldNotCallSaveAllWhenAllBulkItemsFailValidation() {

        ReflectionTestUtils.setField(notificationService, "maxBulkSize", 10);

        CreateNotificationRequest r1 = buildRequest(1L);
        CreateNotificationRequest r2 = buildRequest(2L);

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(new User()));

        when(endpointRepository.findByUserIdAndChannelType(any(), any()))
                .thenReturn(Optional.of(new UserChannelEndpoint()));

        doThrow(new IllegalStateException("Channel disabled"))
                .when(userPreferenceService)
                .validateChannelEnabled(any(), any());

        var response =
                notificationService.createBulkNotifications(List.of(r1, r2));

        assertThat(response.getTotalAccepted()).isEqualTo(0);
        assertThat(response.getTotalRejected()).isEqualTo(2);

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowIfUserNotFoundWhenFetchingNotifications() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.getNotificationsByUser(
                        1L,
                        null,
                        PageRequest.of(0, 10)
                ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldResetScheduledAtIfInPast() {

        mockValidUserAndEndpoint();

        LocalDateTime beforeCall = LocalDateTime.now();

        request.setScheduledAt(beforeCall.minusMinutes(10));

        Notification saved = new Notification();
        saved.setId(1L);
        saved.setStatus(NotificationStatus.CREATED);

        when(notificationRepository.save(any()))
                .thenReturn(saved);

        notificationService.createNotification(request);

        verify(notificationRepository).save(argThat(notification ->
                notification.getScheduledAt().isAfter(beforeCall.minusSeconds(1))
        ));
    }

    @Test
    void shouldKeepScheduledAtIfInFuture() {

        mockValidUserAndEndpoint();

        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(10);
        request.setScheduledAt(futureTime);

        Notification saved = new Notification();
        saved.setId(1L);
        saved.setStatus(NotificationStatus.CREATED);

        when(notificationRepository.save(any()))
                .thenReturn(saved);

        notificationService.createNotification(request);

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification captured = captor.getValue();

        assertThat(captured.getScheduledAt())
                .isEqualTo(futureTime);
    }

    @Test
    void shouldThrowIfBulkRequestIsNull() {

        assertThatThrownBy(() ->
                notificationService.createBulkNotifications(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Bulk request cannot be empty");
    }

    private CreateNotificationRequest buildRequest(Long userId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setChannelType(ChannelType.EMAIL);
        request.setPayload("Test");
        request.setPriority(NotificationPriority.HIGH);
        request.setScheduledAt(LocalDateTime.now());
        return request;
    }

    private void mockValidUserAndEndpoint() {
        mockValidUserAndChannelEndpoint();

        when(retryProperties.getMaxRetries(any()))
                .thenReturn(3);
    }

    private void mockValidUserAndChannelEndpoint() {
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(new User()));

        when(endpointRepository.findByUserIdAndChannelType(any(), any()))
                .thenReturn(Optional.of(new UserChannelEndpoint()));
    }
}
