package com.notification.provider;

public interface SmsProvider {

    void send(String destination, String payload);
}
