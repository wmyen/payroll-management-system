package com.payroll.salary.repository;

import com.payroll.salary.domain.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {
    List<SalaryStructure> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
    Optional<SalaryStructure> findFirstByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
}
