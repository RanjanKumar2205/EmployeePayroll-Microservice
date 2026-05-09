package org.example.employeeservice.dtos;

import lombok.Builder;
import lombok.Data;
import org.example.employeeservice.entities.EmployeeType;
import org.example.employeeservice.entities.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class EmployeeResponseDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfJoining;
    private String designation;
    private EmployeeType employeeType;
    private Status status;
    private LocalDateTime createdAt;
    private Long departmentId;
    private String departmentName;
    private Long managerId;
    private String managerName;
}
