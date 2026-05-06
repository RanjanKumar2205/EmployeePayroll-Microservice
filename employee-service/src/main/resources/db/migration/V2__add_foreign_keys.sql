-- Employee self-ref manager FK
ALTER TABLE employee
    ADD CONSTRAINT FK_EMP_MANAGER    FOREIGN KEY (manager_id)    REFERENCES employee   (id),
    ADD CONSTRAINT FK_EMP_DEPARTMENT FOREIGN KEY (department_id) REFERENCES department (id);

-- Department manager FK (circular, added after employee table exists)
ALTER TABLE department
    ADD CONSTRAINT FK_DEPT_MANAGER FOREIGN KEY (manager_id) REFERENCES employee (id);
