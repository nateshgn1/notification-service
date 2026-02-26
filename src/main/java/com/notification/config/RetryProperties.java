package com.notification.config;

import com.notification.model.enums.ChannelType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "notification.retry")
@Setter
public class RetryProperties {

    private Map<ChannelType, Integer> maxRetries = new HashMap<>();

    public int getMaxRetries(ChannelType type) {
        return maxRetries.getOrDefault(type, 3);
    }
}
