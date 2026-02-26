package com.notification.provider;

public interface EmailProvider {

    void send(String destination, String payload);
}