package com.payroll.salary.repository;

import com.payroll.salary.domain.Allowance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllowanceRepository extends JpaRepository<Allowance, Long> {
}
