package org.example.salaryservice.mappers;

import org.example.salaryservice.dtos.SalaryRequestDto;
import org.example.salaryservice.dtos.SalaryResponseDto;
import org.example.salaryservice.entities.SalaryStructure;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SalaryMapper {

    /** Maps a request DTO to a new (unsaved) SalaryStructure entity. */
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

    /** Maps a persisted SalaryStructure entity to its response DTO. */
    public SalaryResponseDto toResponse(SalaryStructure s) {
        SalaryResponseDto dto = new SalaryResponseDto();
        dto.setId(s.getId());
        dto.setEmployeeId(s.getEmployeeId());
        dto.setBasicSalary(s.getBasicSalary());
        dto.setHra(s.getHra());
        dto.setSpecialAllowance(s.getSpecialAllowance());
        dto.setPfEmployee(s.getPfEmployee());
        dto.setPfEmployer(s.getPfEmployer());
        dto.setProfessionalTax(s.getProfessionalTax());
        dto.setTds(s.getTds());
        dto.setEffectiveFrom(s.getEffectiveFrom());
        dto.setEffectiveTo(s.getEffectiveTo());
        dto.setActive(s.isActive());

        double gross = nullSafeSum(s.getBasicSalary(), s.getHra(), s.getSpecialAllowance());
        double deductions = nullSafeSum(s.getPfEmployee(), s.getProfessionalTax(), s.getTds());
        dto.setGrossSalary(gross);
        dto.setNetSalary(gross - deductions);
        return dto;
    }

    private double nullSafeSum(Double... values) {
        double total = 0;
        for (Double v : values) {
            if (v != null) total += v;
        }
        return total;
    }
}
