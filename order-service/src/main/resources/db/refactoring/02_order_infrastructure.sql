CREATE TABLE IF NOT EXISTS processed_events (
    event_id BINARY(16) NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (event_id),
    INDEX idx_processed_events_processed_at (processed_at)
);

CREATE TABLE IF NOT EXISTS http_idempotency (
    id VARCHAR(64) NOT NULL,
    scope VARCHAR(128) NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    resource_id VARCHAR(128) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_http_idempotency_created_at (created_at)
);
