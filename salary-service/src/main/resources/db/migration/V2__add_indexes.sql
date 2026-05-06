CREATE INDEX IX_SALARY_STRUCTURE_II
    ON salary_structure (employee_id, is_active);

CREATE INDEX IX_SALARY_STRUCTURE_III
    ON salary_structure (effective_from, effective_to);