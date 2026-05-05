package com.payroll.payroll.repository;

import com.payroll.payroll.domain.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {
    Optional<PayrollPeriod> findByYearAndMonth(int year, int month);
    List<PayrollPeriod> findAllByOrderByYearDescMonthDesc();
    List<PayrollPeriod> findByYear(int year);
}
