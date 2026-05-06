-- ============================================================
-- notification_db schema  –  owned by notification-service
-- Tables: notification
-- NOTE: recipient_id is a logical reference to employee-service.
--       No physical FK — resolved via REST if needed.
-- ============================================================

CREATE TABLE IF NOT EXISTS notification (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    recipient_id   BIGINT        DEFAULT NULL COMMENT 'Logical ref to employee-service',
    recipient_email VARCHAR(255) NOT NULL,
    subject        VARCHAR(500)  NOT NULL,
    message        TEXT          NOT NULL,
    channel        ENUM('EMAIL','SMS','IN_APP') NOT NULL DEFAULT 'EMAIL',
    status         ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
    created_at     DATETIME(6)   NOT NULL,
    sent_at        DATETIME(6)   DEFAULT NULL,
    error_message  TEXT          DEFAULT NULL,
    PRIMARY KEY (id),
    KEY IX_NOTIF_RECIPIENT (recipient_id),
    KEY IX_NOTIF_STATUS    (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
