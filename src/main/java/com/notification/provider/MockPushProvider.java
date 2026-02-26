package com.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockPushProvider implements PushProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockPushProvider.class);

    @Override
    public void send(String destination, String payload) {

        logger.info("Mock Push notification sent to destination={}", destination);
        logger.debug("Payload={}", payload);

        simulateRandomFailure();
    }

    private void simulateRandomFailure() {
        if (Math.random() < 0.1) {
            throw new RuntimeException("Simulated Push provider failure");
        }
    }
}
