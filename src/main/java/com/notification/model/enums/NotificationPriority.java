package com.notification.model.enums;

import lombok.Getter;

@Getter
public enum NotificationPriority {

    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int weight;

    NotificationPriority(int weight) {
        this.weight = weight;
    }

}