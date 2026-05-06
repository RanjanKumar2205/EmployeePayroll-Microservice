-- ============================================================
-- salary_db schema  –  owned by salary-service
-- Tables: salary_structure
-- NOTE: employee_id is a logical reference to employee-service.
--       No physical FK — cross-DB references are resolved via REST.
-- ============================================================

CREATE TABLE IF NOT EXISTS salary_structure (
    id                 BIGINT  NOT NULL AUTO_INCREMENT,
    employee_id        BIGINT  NOT NULL COMMENT 'Logical ref to employee-service',
    basic_salary       DOUBLE  DEFAULT NULL,
    hra                DOUBLE  DEFAULT NULL,
    special_allowance  DOUBLE  DEFAULT NULL,
    pf_employee        DOUBLE  DEFAULT NULL,
    pf_employer        DOUBLE  DEFAULT NULL,
    professional_tax   DOUBLE  DEFAULT NULL,
    tds                DOUBLE  DEFAULT NULL,
    effective_from     DATE    DEFAULT NULL,
    effective_to       DATE    DEFAULT NULL,
    is_active          BIT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY IX_SALARY_STRUCTURE_I (employee_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
