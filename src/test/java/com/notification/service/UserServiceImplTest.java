package com.notification.service;

import com.notification.channel.NotificationChannel;
import com.notification.channel.NotificationChannelFactory;
import com.notification.exception.ResourceNotFoundException;
import com.notification.model.dto.request.AddChannelEndpointRequest;
import com.notification.model.dto.request.CreateUserRequest;
import com.notification.model.dto.response.ChannelEndpointResponse;
import com.notification.model.dto.response.UserResponse;
import com.notification.model.entity.User;
import com.notification.model.entity.UserChannelEndpoint;
import com.notification.model.entity.UserPreference;
import com.notification.model.enums.ChannelType;
import com.notification.repository.UserChannelEndpointRepository;
import com.notification.repository.UserPreferenceRepository;
import com.notification.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserChannelEndpointRepository endpointRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private NotificationChannelFactory channelFactory;

    @Mock
    private UserPreferenceRepository preferenceRepository;

    @Mock
    private NotificationChannel channel;

    @Test
    void shouldCreateUserSuccessfully() {

        CreateUserRequest request = new CreateUserRequest();
        request.setPreferredLanguage("en");

        User saved = new User();
        saved.setId(1L);
        saved.setPreferredLanguage("en");

        when(userRepository.save(any(User.class)))
                .thenReturn(saved);

        UserResponse response = userService.createUser(request);

        assertThat(response.getUserId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldAddChannelEndpointSuccessfully() {

        Long userId = 1L;

        AddChannelEndpointRequest request = new AddChannelEndpointRequest();
        request.setChannelType(ChannelType.EMAIL);
        request.setEndpointValue("test@example.com");

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(channelFactory.getChannel(ChannelType.EMAIL))
                .thenReturn(channel);

        when(endpointRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChannelEndpointResponse response =
                userService.addChannelEndpoint(userId, request);

        verify(endpointRepository).save(any());
        assertThat(response.getChannelType()).isEqualTo(ChannelType.EMAIL);
    }

    @Test
    void shouldThrowIfUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        AddChannelEndpointRequest request = new AddChannelEndpointRequest();
        request.setChannelType(ChannelType.EMAIL);

        assertThatThrownBy(() ->
                userService.addChannelEndpoint(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldUpdateExistingEndpoint() {

        Long userId = 1L;

        AddChannelEndpointRequest request = new AddChannelEndpointRequest();
        request.setChannelType(ChannelType.EMAIL);
        request.setEndpointValue("new@example.com");

        User user = new User();
        user.setId(userId);

        UserChannelEndpoint existing = new UserChannelEndpoint();
        existing.setUserId(userId);
        existing.setChannelType(ChannelType.EMAIL);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(endpointRepository.findByUserIdAndChannelType(userId, ChannelType.EMAIL))
                .thenReturn(Optional.of(existing));

        when(channelFactory.getChannel(ChannelType.EMAIL))
                .thenReturn(channel);

        doNothing().when(channel).validateEndpoint(any());

        when(endpointRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChannelEndpointResponse response =
                userService.addChannelEndpoint(userId, request);

        assertThat(response.getEndpointValue()).isEqualTo("new@example.com");
    }

    @Test
    void shouldUpdateExistingPreference() {

        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));

        UserPreference existing = new UserPreference();
        existing.setUserId(userId);
        existing.setChannelType(ChannelType.EMAIL);
        existing.setEnabled(true);

        when(preferenceRepository.findByUserIdAndChannelType(userId, ChannelType.EMAIL))
                .thenReturn(Optional.of(existing));

        userService.updateUserPreference(userId, ChannelType.EMAIL, false);

        verify(preferenceRepository).save(existing);
    }

    @Test
    void shouldCreatePreferenceIfNotExists() {

        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));

        when(preferenceRepository.findByUserIdAndChannelType(userId, ChannelType.EMAIL))
                .thenReturn(Optional.empty());

        userService.updateUserPreference(userId, ChannelType.EMAIL, true);

        verify(preferenceRepository).save(any(UserPreference.class));
    }

    @Test
    void shouldThrowIfUserNotFoundWhenUpdatingPreference() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.updateUserPreference(1L, ChannelType.EMAIL, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
