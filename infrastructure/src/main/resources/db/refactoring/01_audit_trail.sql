CREATE TABLE IF NOT EXISTS audit_trail (
    id BIGINT NOT NULL AUTO_INCREMENT,
    context VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    detail TEXT NULL,
    recorded_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_aggregate (context, aggregate_id, recorded_at)
);
