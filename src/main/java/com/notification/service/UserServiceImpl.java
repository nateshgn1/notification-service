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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final UserChannelEndpointRepository endpointRepository;

    private final UserPreferenceRepository preferenceRepository;

    private final NotificationChannelFactory channelFactory;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Creating user with preferredLanguage={}",
                request.getPreferredLanguage());

        User user = new User();
        user.setPreferredLanguage(request.getPreferredLanguage());

        User saved = userRepository.save(user);

        logger.info("User created successfully. userId={}", saved.getId());

        return UserResponse.builder()
                .userId(saved.getId())
                .preferredLanguage(saved.getPreferredLanguage())
                .build();
    }

    @Override
    @Transactional
    public ChannelEndpointResponse addChannelEndpoint(
            Long userId,
            AddChannelEndpointRequest request
    ) {

        logger.info("Saving channel endpoint. userId={}, channel={}",
                userId,
                request.getChannelType());

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        NotificationChannel channel = channelFactory.getChannel(request.getChannelType());

        channel.validateEndpoint(request.getEndpointValue());

        Optional<UserChannelEndpoint> existingOpt = endpointRepository
                .findByUserIdAndChannelType(
                        userId,
                        request.getChannelType()
                );

        UserChannelEndpoint endpoint;
        boolean isUpdate;

        if (existingOpt.isPresent()) {
            endpoint = existingOpt.get();
            isUpdate = true;
            logger.info("Updating existing endpoint for userId={}, channel={}",
                    userId,
                    request.getChannelType());
        } else {
            endpoint = new UserChannelEndpoint();
            endpoint.setUserId(userId);
            endpoint.setChannelType(request.getChannelType());
            isUpdate = false;
            logger.info("Creating new endpoint for userId={}, channel={}",
                    userId,
                    request.getChannelType());
        }

        endpoint.setEndpointValue(request.getEndpointValue());

        UserChannelEndpoint saved = endpointRepository.save(endpoint);

        logger.info("Channel endpoint {} successfully. userId={}, channel={}",
                isUpdate ? "updated" : "created",
                userId,
                request.getChannelType());

        return ChannelEndpointResponse.builder()
                .userId(saved.getUserId())
                .channelType(saved.getChannelType())
                .endpointValue(saved.getEndpointValue())
                .build();
    }

    @Override
    @Transactional
    public void updateUserPreference(Long userId,
                                     ChannelType channelType,
                                     Boolean enabled) {

        logger.info("Updating preference userId={}, channel={}, enabled={}",
                userId, channelType, enabled);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Optional<UserPreference> existing = preferenceRepository.findByUserIdAndChannelType(userId, channelType);

        UserPreference preference;

        if (existing.isPresent()) {
            preference = existing.get();
            logger.info("Updating existing preference userId={}, channel={}",
                    userId, channelType);
        } else {
            // Default model → only create row when user explicitly changes preference
            preference = new UserPreference();
            preference.setUserId(userId);
            preference.setChannelType(channelType);
            logger.info("Creating new preference row userId={}, channel={}",
                    userId, channelType);
        }

        preference.setEnabled(enabled);

        preferenceRepository.save(preference);

        logger.info("Preference updated successfully userId={}, channel={}",
                userId, channelType);
    }
}
