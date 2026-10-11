INSERT INTO http_idempotency (id, scope, idempotency_key, request_hash, resource_id, created_at)
SELECT
    SHA2(CONCAT(CHAR_LENGTH('POST /orders'), ':', 'POST /orders', idempotency_key), 256),
    'POST /orders',
    idempotency_key,
    request_hash,
    BIN_TO_UUID(order_id),
    created_at
FROM order_idempotency_keys
ON DUPLICATE KEY UPDATE id = http_idempotency.id;
