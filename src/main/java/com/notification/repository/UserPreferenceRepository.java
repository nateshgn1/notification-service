package com.notification.repository;

import com.notification.model.entity.UserPreference;
import com.notification.model.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    Optional<UserPreference> findByUserIdAndChannelType(
            Long userId,
            ChannelType channelType
    );
}