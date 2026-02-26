CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    payload TEXT NOT NULL,
    content_type VARCHAR(50) NOT NULL DEFAULT 'text/plain',
    priority VARCHAR(10) NOT NULL,
    priority_weight INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    scheduled_at TIMESTAMP NULL,
    recurrence_interval_minutes BIGINT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE INDEX idx_notification_ready
ON notifications (status, scheduled_at, priority_weight, created_at);

CREATE INDEX idx_notification_retry
ON notifications (status, next_retry_at, priority_weight, created_at);

CREATE INDEX idx_notification_user_created
ON notifications (user_id, created_at);

CREATE INDEX idx_notification_user_status_created
ON notifications (user_id, status, created_at);