package com.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockSmsProvider implements SmsProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockSmsProvider.class);

    @Override
    public void send(String destination, String payload) {

        logger.info("Mock SMS sent to destination={}", destination);
        logger.debug("Payload={}", payload);

        simulateRandomFailure();
    }

    private void simulateRandomFailure() {
        if (Math.random() < 0.15) {
            throw new RuntimeException("Simulated SMS provider failure");
        }
    }
}