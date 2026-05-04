package com.payroll.payroll.service;

import com.payroll.attendance.domain.LeaveRequest;
import com.payroll.attendance.domain.LeaveType;
import com.payroll.attendance.repository.LeaveRequestRepository;
import com.payroll.attendance.repository.OvertimeRecordRepository;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.payroll.domain.PayrollItem;
import com.payroll.payroll.domain.PayrollRecord;
import com.payroll.payroll.domain.PayrollRecordStatus;
import com.payroll.payroll.domain.TaxBracket;
import com.payroll.payroll.repository.PayrollItemRepository;
import com.payroll.payroll.repository.PayrollRecordRepository;
import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.repository.SalaryStructureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollCalculationService {

    // Insurance rates
    private static final BigDecimal LABOR_RATE = new BigDecimal("0.11");
    private static final BigDecimal EMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.01");
    private static final BigDecimal OCCUPATIONAL_RATE = new BigDecimal("0.002");
    private static final BigDecimal TOTAL_EMPLOYEE_LABOR_SHARE = new BigDecimal("0.20");
    private static final BigDecimal TOTAL_EMPLOYER_LABOR_SHARE = new BigDecimal("0.70");

    private static final BigDecimal HEALTH_RATE = new BigDecimal("0.0517");
    private static final BigDecimal HEALTH_EMPLOYEE_SHARE = new BigDecimal("0.30");
    private static final BigDecimal HEALTH_EMPLOYER_SHARE = new BigDecimal("0.60");

    private static final BigDecimal PENSION_RATE = new BigDecimal("0.06");
    private static final BigDecimal DAYS_PER_MONTH = new BigDecimal("30");

    private final PayrollRecordRepository payrollRecordRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TaxBracketService taxBracketService;

    @Transactional
    public List<PayrollRecord> calculateForPeriod(Long periodId, int year, int month,
                                                   LocalDate periodStart, LocalDate periodEnd) {
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .toList();

        for (Employee emp : employees) {
            PayrollRecord existing = payrollRecordRepository.findByPeriodIdAndEmployeeIdIn(
                    periodId, List.of(emp.getId()))
                    .stream().findFirst().orElse(null);

            if (existing != null && existing.getStatus() == PayrollRecordStatus.CONFIRMED) {
                log.info("Skipping confirmed record for employee {}", emp.getId());
                continue;
            }

            PayrollRecord record = existing != null ? existing :
                    PayrollRecord.builder().periodId(periodId).employeeId(emp.getId()).build();

            calculateEmployeePayroll(record, emp, year, month, periodStart, periodEnd);

            // Re-accumulate custom items
            List<PayrollItem> items = payrollItemRepository.findByPayrollRecordId(record.getId());
            BigDecimal otherEarnings = items.stream()
                    .filter(i -> "EARNING".equals(i.getItemType().name()))
                    .map(PayrollItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal otherDeductions = items.stream()
                    .filter(i -> "DEDUCTION".equals(i.getItemType().name()))
                    .map(PayrollItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            record.setCalculationResult(
                    record.getBaseSalary(), record.getTotalAllowances(),
                    record.getOvertimePay(), otherEarnings,
                    record.getGrossPay().add(otherEarnings),
                    record.getLaborInsurance(), record.getHealthInsurance(),
                    record.getIncomeTax(), record.getLeaveDeduction(),
                    otherDeductions,
                    record.getLaborInsurance().add(record.getHealthInsurance())
                            .add(record.getIncomeTax()).add(record.getLeaveDeduction()).add(otherDeductions),
                    record.getGrossPay().add(otherEarnings)
                            .subtract(record.getLaborInsurance()).subtract(record.getHealthInsurance())
                            .subtract(record.getIncomeTax()).subtract(record.getLeaveDeduction()).subtract(otherDeductions),
                    record.getEmployerLaborIns(), record.getEmployerHealthIns(),
                    record.getEmployerPension(), record.getTotalEmployerCost()
            );

            payrollRecordRepository.save(record);
        }

        return payrollRecordRepository.findByPeriodId(periodId);
    }

    void calculateEmployeePayroll(PayrollRecord record, Employee employee,
                                   int year, int month,
                                   LocalDate periodStart, LocalDate periodEnd) {
        // 1. Get salary structure
        SalaryStructure salary = salaryStructureRepository
                .findByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        employee.getId(), periodStart)
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Salary structure not found for employee " + employee.getId()));

        BigDecimal baseSalary = salary.getBaseSalary();
        BigDecimal totalAllowances = salary.getTotalAllowances();

        // 2. Overtime pay from approved records in period
        BigDecimal overtimePay = overtimeRecordRepository
                .sumApprovedPayByEmployeeAndPeriod(employee.getId(), periodStart, periodEnd);

        // 3. Leave deduction
        BigDecimal leaveDeduction = calculateLeaveDeduction(employee.getId(), baseSalary,
                periodStart, periodEnd);

        // 4. Gross pay (before custom items)
        BigDecimal grossPay = baseSalary.add(totalAllowances).add(overtimePay);

        // 5. Insurance
        BigDecimal insuredSalary = baseSalary;
        BigDecimal laborInsurance = insuredSalary
                .multiply(LABOR_RATE.add(EMPLOYMENT_INSURANCE_RATE))
                .multiply(TOTAL_EMPLOYEE_LABOR_SHARE)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal healthInsurance = insuredSalary
                .multiply(HEALTH_RATE)
                .multiply(HEALTH_EMPLOYEE_SHARE)
                .setScale(0, RoundingMode.HALF_UP);

        // 6. Income tax
        BigDecimal taxableIncome = grossPay.subtract(laborInsurance).subtract(healthInsurance);
        BigDecimal incomeTax = taxBracketService.calculateTax(year, taxableIncome);

        // 7. Net pay
        BigDecimal totalDeductions = laborInsurance.add(healthInsurance).add(incomeTax).add(leaveDeduction);
        BigDecimal netPay = grossPay.subtract(totalDeductions);

        // 8. Employer costs
        BigDecimal employerLaborIns = insuredSalary
                .multiply(LABOR_RATE.add(EMPLOYMENT_INSURANCE_RATE).add(OCCUPATIONAL_RATE))
                .multiply(TOTAL_EMPLOYER_LABOR_SHARE)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal employerHealthIns = insuredSalary
                .multiply(HEALTH_RATE)
                .multiply(HEALTH_EMPLOYER_SHARE)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal employerPension = insuredSalary.multiply(PENSION_RATE)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalEmployerCost = grossPay.add(employerLaborIns).add(employerHealthIns).add(employerPension);

        record.setCalculationResult(
                baseSalary, totalAllowances, overtimePay, BigDecimal.ZERO, grossPay,
                laborInsurance, healthInsurance, incomeTax, leaveDeduction, BigDecimal.ZERO,
                totalDeductions, netPay,
                employerLaborIns, employerHealthIns, employerPension, totalEmployerCost
        );
    }

    private BigDecimal calculateLeaveDeduction(Long employeeId, BigDecimal baseSalary,
                                                LocalDate periodStart, LocalDate periodEnd) {
        List<LeaveRequest> leaves = leaveRequestRepository
                .findApprovedInRange(employeeId, periodStart, periodEnd);

        if (leaves.isEmpty()) return BigDecimal.ZERO;

        BigDecimal dailyRate = baseSalary.divide(DAYS_PER_MONTH, 2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = BigDecimal.ZERO;

        for (LeaveRequest leave : leaves) {
            // Count days within the period
            LocalDate leaveStart = leave.getStartDate().isBefore(periodStart) ? periodStart : leave.getStartDate();
            LocalDate leaveEnd = leave.getEndDate().isAfter(periodEnd) ? periodEnd : leave.getEndDate();

            if (leaveStart.isAfter(leaveEnd)) continue;

            BigDecimal daysInPeriod = BigDecimal.valueOf(
                    java.time.temporal.ChronoUnit.DAYS.between(leaveStart, leaveEnd) + 1);

            totalDeduction = totalDeduction.add(
                    switch (leave.getLeaveType()) {
                        case PERSONAL -> dailyRate.multiply(daysInPeriod);
                        case SICK -> dailyRate.multiply(daysInPeriod)
                                .multiply(new BigDecimal("0.5"));
                        default -> BigDecimal.ZERO; // paid leave
                    }
            );
        }

        return totalDeduction.setScale(0, RoundingMode.HALF_UP);
    }

    @Transactional
    public PayrollRecord recalculateSingle(Long recordId) {
        PayrollRecord record = payrollRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Payroll record not found: " + recordId));
        if (record.getStatus() == PayrollRecordStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot recalculate a confirmed record");
        }

        Employee employee = employeeRepository.findById(record.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // We don't have period info here directly, use record's period
        // The caller (controller) should provide year/month/periodStart/periodEnd
        throw new UnsupportedOperationException("Use calculateForPeriod instead");
    }
}
