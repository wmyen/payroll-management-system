package com.payroll.payroll.service;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.payroll.domain.PayrollPeriod;
import com.payroll.payroll.domain.PayrollRecord;
import com.payroll.payroll.domain.WithholdingStatement;
import com.payroll.payroll.domain.WithholdingStatus;
import com.payroll.payroll.repository.PayrollPeriodRepository;
import com.payroll.payroll.repository.PayrollRecordRepository;
import com.payroll.payroll.repository.WithholdingStatementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithholdingService {

    private final WithholdingStatementRepository withholdingStatementRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final EmployeeRepository employeeRepository;

    public List<WithholdingStatement> getByYear(int year) {
        return withholdingStatementRepository.findByYearOrderByEmployeeId(year);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getById(Long id) {
        WithholdingStatement stmt = withholdingStatementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Withholding statement not found: " + id));

        Employee emp = employeeRepository.findById(stmt.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (emp.getDepartment() != null) {
            emp.getDepartment().getName();
        }

        Map<String, Object> empInfo = new java.util.HashMap<>();
        empInfo.put("id", emp.getId());
        empInfo.put("name", emp.getName());
        empInfo.put("department", emp.getDepartment() != null
                ? Map.of("name", emp.getDepartment().getName()) : null);

        return Map.of("statement", stmt, "employee", empInfo);
    }

    @Transactional
    public List<WithholdingStatement> generateForYear(int year) {
        // Find all confirmed/locked periods for the year
        List<PayrollPeriod> periods = payrollPeriodRepository.findByYear(year);

        // Get all payroll records from those periods
        List<PayrollRecord> allRecords = periods.stream()
                .flatMap(p -> payrollRecordRepository.findByPeriodId(p.getId()).stream())
                .collect(Collectors.toList());

        // Group by employee
        Map<Long, List<PayrollRecord>> byEmployee = allRecords.stream()
                .collect(Collectors.groupingBy(PayrollRecord::getEmployeeId));

        for (Map.Entry<Long, List<PayrollRecord>> entry : byEmployee.entrySet()) {
            Long empId = entry.getKey();
            List<PayrollRecord> records = entry.getValue();

            WithholdingStatement stmt = withholdingStatementRepository
                    .findByYearAndEmployeeId(year, empId)
                    .orElseGet(() -> WithholdingStatement.builder()
                            .year(year)
                            .employeeId(empId)
                            .build());

            if (stmt.getStatus() == WithholdingStatus.CONFIRMED) {
                log.info("Skipping confirmed withholding for employee {}", empId);
                continue;
            }

            BigDecimal totalGross = records.stream().map(PayrollRecord::getGrossPay).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalLaborIns = records.stream().map(PayrollRecord::getLaborInsurance).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalHealthIns = records.stream().map(PayrollRecord::getHealthInsurance).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalTax = records.stream().map(PayrollRecord::getIncomeTax).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalNet = records.stream().map(PayrollRecord::getNetPay).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalEmployerCost = records.stream().map(PayrollRecord::getTotalEmployerCost).reduce(BigDecimal.ZERO, BigDecimal::add);

            stmt.updateTotals(totalGross, totalLaborIns, totalHealthIns, totalTax, totalNet, totalEmployerCost, records.size());

            withholdingStatementRepository.save(stmt);
        }

        return withholdingStatementRepository.findByYearOrderByEmployeeId(year);
    }

    @Transactional
    public void confirm(Long id) {
        WithholdingStatement stmt = withholdingStatementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Withholding statement not found: " + id));
        stmt.confirm();
        withholdingStatementRepository.save(stmt);
    }

    @Transactional
    public void confirmAll(int year) {
        List<WithholdingStatement> drafts = withholdingStatementRepository
                .findByYearAndStatus(year, WithholdingStatus.DRAFT);
        for (WithholdingStatement stmt : drafts) {
            stmt.confirm();
        }
        withholdingStatementRepository.saveAll(drafts);
    }
}
