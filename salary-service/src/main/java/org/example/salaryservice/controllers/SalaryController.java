package org.example.salaryservice.controllers;

import jakarta.validation.Valid;
import org.example.salaryservice.dtos.SalaryRequestDto;
import org.example.salaryservice.dtos.SalaryResponseDto;
import org.example.salaryservice.services.SalaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping
    public ResponseEntity<List<SalaryResponseDto>> getAll(
            @RequestParam(required = false) Long employeeId) {
        if (employeeId != null) {
            return ResponseEntity.ok(salaryService.getByEmployeeId(employeeId));
        }
        return ResponseEntity.ok(salaryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salaryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SalaryResponseDto> add(@Valid @RequestBody SalaryRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaryService.addSalary(dto));
    }

    @PostMapping("/revise/{employeeId}")
    public ResponseEntity<SalaryResponseDto> revise(@PathVariable Long employeeId,
                                                    @Valid @RequestBody SalaryRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salaryService.reviseSalary(employeeId, dto));
    }
}
