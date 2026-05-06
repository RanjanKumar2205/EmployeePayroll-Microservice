package org.example.employeeservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.employeeservice.entities.EmployeeType;

import java.time.LocalDate;

@Data
public class EmployeeRequestDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email @NotBlank
    private String email;
    private String phoneNumber;
    private LocalDate dateOfJoining;
    private String designation;
    private EmployeeType employeeType;
    private String employeeCode;
    private Long departmentId;
    private Long managerId;
}
