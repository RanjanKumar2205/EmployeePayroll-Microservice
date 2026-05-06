-- ============================================================
-- employee_db schema  –  owned by employee-service
-- Tables: department, employee
-- ============================================================

CREATE TABLE IF NOT EXISTS department (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    code       VARCHAR(255) DEFAULT NULL,
    name       VARCHAR(255) DEFAULT NULL,
    status     ENUM('ACTIVE','DELETE','HOLD','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    manager_id BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS employee (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  DEFAULT NULL,
    created_by       VARCHAR(255) DEFAULT NULL,
    last_modified_by VARCHAR(255) DEFAULT NULL,
    date_of_joining  DATE         DEFAULT NULL,
    designation      VARCHAR(255) DEFAULT NULL,
    email            VARCHAR(255) NOT NULL,
    employee_code    VARCHAR(255) DEFAULT NULL,
    employee_type    ENUM('CONTRACT','FULL_TIME','PART_TIME','INTERN') DEFAULT NULL,
    first_name       VARCHAR(255) DEFAULT NULL,
    last_name        VARCHAR(255) DEFAULT NULL,
    phone_number     VARCHAR(255) DEFAULT NULL,
    status           ENUM('ACTIVE','INACTIVE','HOLD','DELETE') DEFAULT 'ACTIVE',
    department_id    BIGINT       DEFAULT NULL,
    manager_id       BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_EMPLOYEE_EMAIL (email),
    KEY IX_EMPLOYEE_I (department_id, status),
    KEY IX_EMPLOYEE_II (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
