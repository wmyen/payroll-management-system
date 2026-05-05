package com.payroll.payroll.repository;

import com.payroll.payroll.domain.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    List<PayrollRecord> findByPeriodId(Long periodId);
    List<PayrollRecord> findByPeriodIdAndEmployeeIdIn(Long periodId, List<Long> employeeIds);
    List<PayrollRecord> findByPeriodIdIn(List<Long> periodIds);
    List<PayrollRecord> findByEmployeeIdOrderByPeriodIdDesc(Long employeeId);
}
