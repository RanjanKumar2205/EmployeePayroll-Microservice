package org.example.salaryservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "salary_structure")
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Logical reference — physical FK deliberately omitted (different DB). */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    private Double basicSalary;
    private Double hra;
    private Double specialAllowance;
    private Double pfEmployee;
    private Double pfEmployer;
    private Double professionalTax;
    private Double tds;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
