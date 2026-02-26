package com.notification.service;

import com.notification.model.enums.ChannelType;
import com.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;

    @Override
    public void validateChannelEnabled(Long userId, ChannelType channelType) {

        preferenceRepository.findByUserIdAndChannelType(userId, channelType)
                .ifPresent(pref -> {
                    if (!Boolean.TRUE.equals(pref.getEnabled())) {
                        throw new IllegalStateException(
                                "Channel disabled for user"
                        );
                    }
                });
    }
}
