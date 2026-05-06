package org.example.salaryservice.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SalaryRequestDto {

    @NotNull
    private Long employeeId;

    @Positive
    private Double basicSalary;

    private Double hra;
    private Double specialAllowance;
    private Double pfEmployee;
    private Double pfEmployer;
    private Double professionalTax;
    private Double tds;
    private LocalDate effectiveFrom;
}
