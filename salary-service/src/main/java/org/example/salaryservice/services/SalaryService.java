package org.example.salaryservice.services;

import jakarta.validation.Valid;
import org.example.salaryservice.dtos.EmployeeResponseDto;
import org.example.salaryservice.dtos.SalaryRequestDto;
import org.example.salaryservice.dtos.SalaryResponseDto;
import org.example.salaryservice.entities.SalaryStructure;
import org.example.salaryservice.exceptions.DuplicateResourceException;
import org.example.salaryservice.exceptions.ResourceNotFoundException;
import org.example.salaryservice.feign.EmployeeClient;
import org.example.salaryservice.mappers.SalaryMapper;
import org.example.salaryservice.repositories.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final SalaryMapper salaryMapper;
    private final EmployeeClient employeeClient;

    public SalaryService(SalaryRepository salaryRepository, SalaryMapper salaryMapper, EmployeeClient employeeClient) {
        this.salaryRepository = salaryRepository;
        this.salaryMapper = salaryMapper;
        this.employeeClient = employeeClient;
    }

    public List<SalaryResponseDto> getAll() {
        return salaryRepository.findAll().stream()
                .map(salaryMapper::toResponse)
                .toList();
    }

    public List<SalaryResponseDto> getByEmployeeId(Long employeeId) {
        return salaryRepository.findByEmployeeId(employeeId).stream()
                .map(salaryMapper::toResponse)
                .toList();
    }

    public SalaryResponseDto getById(Long id) {
        return salaryMapper.toResponse(findById(id));
    }

    @Transactional
    public SalaryResponseDto addSalary(@Valid SalaryRequestDto dto) {
        if(getEmployeeById(dto.getEmployeeId()) == null) {throw new ResourceNotFoundException("Employee not found with id " + dto.getEmployeeId());}
        salaryRepository.findByEmployeeIdAndIsActiveTrue(dto.getEmployeeId())
                .ifPresent(s -> {
                    throw new DuplicateResourceException(
                            "Active salary already exists for employee " + dto.getEmployeeId()
                                    + ". Use the /revise endpoint.");
                });
        return salaryMapper.toResponse(salaryRepository.save(salaryMapper.toEntity(dto)));
    }

    @Transactional
    public SalaryResponseDto reviseSalary(Long employeeId, @Valid SalaryRequestDto dto) {
        SalaryStructure current = salaryRepository
                .findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active salary found for employee: " + employeeId));

        current.setActive(false);
        current.setEffectiveTo(LocalDate.now());
        salaryRepository.save(current);

        SalaryStructure next = salaryMapper.toEntity(dto);
        next.setEmployeeId(employeeId);
        return salaryMapper.toResponse(salaryRepository.save(next));
    }

    private SalaryStructure findById(Long id) {
        return salaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary not found: " + id));
    }

    public EmployeeResponseDto getEmployeeById(Long id) {
        return employeeClient.getById(id).getBody();
    }
}
