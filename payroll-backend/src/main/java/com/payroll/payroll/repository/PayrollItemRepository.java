package com.payroll.payroll.repository;

import com.payroll.payroll.domain.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {
    List<PayrollItem> findByPayrollRecordId(Long payrollRecordId);
    void deleteByPayrollRecordId(Long payrollRecordId);
}
