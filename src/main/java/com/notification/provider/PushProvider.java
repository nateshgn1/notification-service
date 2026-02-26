package com.notification.provider;

public interface PushProvider {

    void send(String destination, String payload);
}
