package com.payroll.payroll.repository;

import com.payroll.payroll.domain.InsuranceRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InsuranceRateRepository extends JpaRepository<InsuranceRate, Long> {

    Optional<InsuranceRate> findTopByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(LocalDate date);

    List<InsuranceRate> findAllByOrderByEffectiveDateDesc();
}
