package com.notification.repository;

import com.notification.model.entity.UserChannelEndpoint;
import com.notification.model.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserChannelEndpointRepository extends JpaRepository<UserChannelEndpoint, Long> {

    Optional<UserChannelEndpoint> findByUserIdAndChannelType(
            Long userId,
            ChannelType channelType
    );
}
