package org.example.employeeservice.services;

import jakarta.validation.Valid;
import org.example.employeeservice.dtos.EmployeeCreatedEvent;
import org.example.employeeservice.dtos.EmployeeRequestDto;
import org.example.employeeservice.dtos.EmployeeResponseDto;
import org.example.employeeservice.entities.Department;
import org.example.employeeservice.entities.Employee;
import org.example.employeeservice.entities.Status;
import org.example.employeeservice.exceptions.ResourceNotFoundException;
import org.example.employeeservice.mappers.EmployeeMapper;
import org.example.employeeservice.repositories.DepartmentRepository;
import org.example.employeeservice.repositories.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EmployeeService {
    private final KafkaTemplate<String, EmployeeCreatedEvent> kafkaTemplate;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           EmployeeMapper employeeMapper,
                           KafkaTemplate<String, EmployeeCreatedEvent> kafkaTemplate) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeMapper = employeeMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Page<EmployeeResponseDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
    }

    public EmployeeResponseDto getEmployeeById(Long id) {
        return employeeMapper.toResponse(findById(id));
    }

    @Transactional
    public EmployeeResponseDto createEmployee(@Valid EmployeeRequestDto dto) {
        Employee emp = employeeMapper.toEntity(dto);

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + dto.getDepartmentId()));
            emp.setDepartment(dept);
        }
        if (dto.getManagerId() != null) {
            emp.setManager(findById(dto.getManagerId()));
        }

        Employee savedEmployee = employeeRepository.save(emp);

        EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                savedEmployee.getId(),
                savedEmployee.getName(),
                savedEmployee.getEmail(),
                Instant.now()
        );

        kafkaTemplate.send("employee-created", event);

        return employeeMapper.toResponse(savedEmployee);
    }

    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, @Valid EmployeeRequestDto dto) {
        Employee emp = findById(id);
        employeeMapper.updateEntity(emp, dto);
        return employeeMapper.toResponse(employeeRepository.save(emp));
    }

    @Transactional
    public void deactivateEmployee(Long id) {
        Employee emp = findById(id);
        if(Status.INACTIVE.equals(emp.getStatus())) {
            throw new ResourceNotFoundException("Employee not found: " + id);
        }
        emp.setStatus(Status.INACTIVE);
        employeeRepository.save(emp);
    }

    private Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }
}
