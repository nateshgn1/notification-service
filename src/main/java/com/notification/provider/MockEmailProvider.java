package com.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockEmailProvider implements EmailProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailProvider.class);

    @Override
    public void send(String destination, String payload) {

        logger.info("Mock Email notification sent to destination={}", destination);
        logger.debug("Payload={}", payload);

        simulateRandomFailure();
    }

    private void simulateRandomFailure() {
        if (Math.random() < 0.2) {
            throw new RuntimeException("Simulated email provider failure");
        }
    }
}