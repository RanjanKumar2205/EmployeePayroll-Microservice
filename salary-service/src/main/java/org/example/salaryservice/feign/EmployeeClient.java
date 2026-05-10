package org.example.salaryservice.feign;

import org.example.salaryservice.dtos.EmployeeResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="employee-service")
public interface EmployeeClient {
    @GetMapping("/api/v1/employees/{id}")
    public ResponseEntity<EmployeeResponseDto> getById(@PathVariable Long id);
}
