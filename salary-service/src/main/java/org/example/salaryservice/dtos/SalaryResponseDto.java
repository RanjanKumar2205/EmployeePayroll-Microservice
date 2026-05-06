package org.example.salaryservice.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SalaryResponseDto {
    private Long id;
    private Long employeeId;
    private Double basicSalary;
    private Double hra;
    private Double specialAllowance;
    private Double pfEmployee;
    private Double pfEmployer;
    private Double professionalTax;
    private Double tds;
    private Double grossSalary;
    private Double netSalary;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean isActive;
}
