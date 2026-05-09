package org.example.salaryservice.mappers;

import org.example.salaryservice.dtos.SalaryRequestDto;
import org.example.salaryservice.dtos.SalaryResponseDto;
import org.example.salaryservice.entities.SalaryStructure;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SalaryMapper {

    public SalaryStructure toEntity(SalaryRequestDto dto) {
        return SalaryStructure.builder()
                .employeeId(dto.getEmployeeId())
                .basicSalary(dto.getBasicSalary())
                .hra(dto.getHra())
                .specialAllowance(dto.getSpecialAllowance())
                .pfEmployee(dto.getPfEmployee())
                .pfEmployer(dto.getPfEmployer())
                .professionalTax(dto.getProfessionalTax())
                .tds(dto.getTds())
                .effectiveFrom(dto.getEffectiveFrom() != null
                        ? dto.getEffectiveFrom()
                        : LocalDate.now())
                .build();
    }

    public SalaryResponseDto toResponse(SalaryStructure s) {
        double gross = nullSafeSum(s.getBasicSalary(), s.getHra(), s.getSpecialAllowance());
        double deductions = nullSafeSum(s.getPfEmployee(), s.getProfessionalTax(), s.getTds());

        return SalaryResponseDto.builder()
                .id(s.getId())
                .employeeId(s.getEmployeeId())
                .basicSalary(s.getBasicSalary())
                .hra(s.getHra())
                .specialAllowance(s.getSpecialAllowance())
                .pfEmployee(s.getPfEmployee())
                .pfEmployer(s.getPfEmployer())
                .professionalTax(s.getProfessionalTax())
                .tds(s.getTds())
                .effectiveFrom(s.getEffectiveFrom())
                .effectiveTo(s.getEffectiveTo())
                .isActive(s.isActive())
                .grossSalary(gross)
                .netSalary(gross - deductions)
                .build();
    }

    private double nullSafeSum(Double... values) {
        double total = 0;
        for (Double v : values) {
            if (v != null) total += v;
        }
        return total;
    }
}
