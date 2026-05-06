package org.example.salaryservice.repositories;

import org.example.salaryservice.entities.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<SalaryStructure, Long> {

    List<SalaryStructure> findByEmployeeId(Long employeeId);

    Optional<SalaryStructure> findByEmployeeIdAndIsActiveTrue(Long employeeId);
}
