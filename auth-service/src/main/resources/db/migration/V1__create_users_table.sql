-- V1__create_users_table.sql
-- Stores registered users for the auth-service.
-- Passwords are stored as BCrypt hashes (never plain-text).

CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username     VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,  -- BCrypt hash
    role         ENUM('ADMIN','HR','EMPLOYEE','GUEST') NOT NULL DEFAULT 'GUEST',
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    is_protected    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6),

    PRIMARY KEY (id),
    CONSTRAINT UQ_USERS_USERNAME UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
