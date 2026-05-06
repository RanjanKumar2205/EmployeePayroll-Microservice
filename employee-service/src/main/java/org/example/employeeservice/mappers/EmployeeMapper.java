package org.example.employeeservice.mappers;

import org.example.employeeservice.dtos.EmployeeRequestDto;
import org.example.employeeservice.dtos.EmployeeResponseDto;
import org.example.employeeservice.entities.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    /**
     * Maps a validated request DTO to a new (unsaved) Employee entity.
     * Relational fields (department, manager) are resolved by the service
     * because they require repository lookups; this method sets only the
     * scalar fields that come directly from the DTO.
     */
    public Employee toEntity(EmployeeRequestDto dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .dateOfJoining(dto.getDateOfJoining())
                .designation(dto.getDesignation())
                .employeeType(dto.getEmployeeType())
                .employeeCode(dto.getEmployeeCode())
                .build();
    }

    /**
     * Applies scalar fields from the request DTO onto an existing entity
     * (used for PUT updates). Relational fields are handled by the service.
     */
    public void updateEntity(Employee target, EmployeeRequestDto dto) {
        target.setFirstName(dto.getFirstName());
        target.setLastName(dto.getLastName());
        target.setEmail(dto.getEmail());
        target.setPhoneNumber(dto.getPhoneNumber());
        target.setDateOfJoining(dto.getDateOfJoining());
        target.setDesignation(dto.getDesignation());
        target.setEmployeeType(dto.getEmployeeType());
        target.setEmployeeCode(dto.getEmployeeCode());
    }

    /** Maps a persisted Employee entity to its response DTO. */
    public EmployeeResponseDto toResponse(Employee emp) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(emp.getId());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setFirstName(emp.getFirstName());
        dto.setLastName(emp.getLastName());
        dto.setEmail(emp.getEmail());
        dto.setPhoneNumber(emp.getPhoneNumber());
        dto.setDateOfJoining(emp.getDateOfJoining());
        dto.setDesignation(emp.getDesignation());
        dto.setEmployeeType(emp.getEmployeeType());
        dto.setStatus(emp.getStatus());
        dto.setCreatedAt(emp.getCreatedAt());

        if (emp.getDepartment() != null) {
            dto.setDepartmentId(emp.getDepartment().getId());
            dto.setDepartmentName(emp.getDepartment().getName());
        }
        if (emp.getManager() != null) {
            dto.setManagerId(emp.getManager().getId());
            dto.setManagerName(emp.getManager().getFirstName() + " " + emp.getManager().getLastName());
        }
        return dto;
    }
}
