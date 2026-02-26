package com.notification.channel;

import com.notification.exception.BadRequestException;
import com.notification.model.enums.ChannelType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationChannelFactory {

    private final Map<ChannelType, NotificationChannel> channelMap;

    public NotificationChannelFactory(List<NotificationChannel> channels) {

        this.channelMap = channels.stream()
                .collect(Collectors.toMap(
                        NotificationChannel::getSupportedChannel,
                        Function.identity()
                ));
    }

    public NotificationChannel getChannel(ChannelType channelType) {

        NotificationChannel channel = channelMap.get(channelType);

        if (channel == null) {
            throw new BadRequestException(
                    "Unsupported channel: " + channelType);
        }

        return channel;
    }
}
