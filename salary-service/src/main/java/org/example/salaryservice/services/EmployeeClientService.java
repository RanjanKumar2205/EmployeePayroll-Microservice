package org.example.salaryservice.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.example.salaryservice.dtos.EmployeeResponseDto;
import org.example.salaryservice.feign.EmployeeClient;
import org.springframework.stereotype.Service;

@Service
public class EmployeeClientService {

    private final EmployeeClient employeeClient;

    public EmployeeClientService(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    @CircuitBreaker(name = "employee-service", fallbackMethod = "fallbackGetEmployee")
    @Retry(name = "employee-service")
    public EmployeeResponseDto getEmployee(Long id) {
        return employeeClient.getById(id).getBody();
    }

    public EmployeeResponseDto fallbackGetEmployee(Long id, Throwable t) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "employee-service is unavailable for employee: " + id
        );
    }
}