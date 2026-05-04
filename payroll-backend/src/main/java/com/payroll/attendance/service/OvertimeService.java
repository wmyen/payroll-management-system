package com.payroll.attendance.service;

import com.payroll.attendance.domain.*;
import com.payroll.attendance.dto.OvertimeRequestDto;
import com.payroll.attendance.repository.OvertimeRecordRepository;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.repository.SalaryStructureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class OvertimeService {

    private static final BigDecimal MAX_MONTHLY_HOURS = new BigDecimal("46");
    private static final BigDecimal EXTENDED_MAX_HOURS = new BigDecimal("54");
    private static final BigDecimal HOURS_PER_MONTH = new BigDecimal("240");

    private static final BigDecimal RATE_WORKDAY_FIRST_2 = new BigDecimal("1.33");
    private static final BigDecimal RATE_WORKDAY_AFTER_2 = new BigDecimal("1.66");
    private static final BigDecimal RATE_REST_FIRST_2 = new BigDecimal("1.33");
    private static final BigDecimal RATE_REST_2_TO_8 = new BigDecimal("1.66");
    private static final BigDecimal RATE_REST_OVER_8 = new BigDecimal("2.66");
    private static final BigDecimal RATE_HOLIDAY = new BigDecimal("2.0");

    private final OvertimeRecordRepository overtimeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryStructureRepository salaryStructureRepository;

    public OvertimeRecord getById(Long id) {
        return overtimeRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Overtime record not found: " + id));
    }

    public Page<OvertimeRecord> search(Long employeeId, Long departmentId,
                                        LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return overtimeRecordRepository.search(employeeId, departmentId, startDate, endDate, pageable);
    }

    @Transactional
    public OvertimeRecord create(OvertimeRequestDto request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + request.getEmployeeId()));

        BigDecimal hours = calculateHours(request.getStartTime(), request.getEndTime());

        // Check monthly overtime limit
        LocalDate monthStart = request.getOvertimeDate().withDayOfMonth(1);
        LocalDate monthEnd = request.getOvertimeDate().withDayOfMonth(
                request.getOvertimeDate().lengthOfMonth());
        BigDecimal currentHours = overtimeRecordRepository
                .sumApprovedHoursByEmployeeAndMonth(employee.getId(), monthStart, monthEnd, OvertimeStatus.APPROVED);
        if (currentHours.add(hours).compareTo(MAX_MONTHLY_HOURS) > 0) {
            throw new IllegalArgumentException(
                    "Monthly overtime limit exceeded. Current: " + currentHours + " + " + hours + " > 46 hours");
        }

        OvertimeRecord record = OvertimeRecord.builder()
                .employee(employee)
                .overtimeDate(request.getOvertimeDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .hours(hours)
                .overtimeType(OvertimeType.valueOf(request.getOvertimeType()))
                .build();

        return overtimeRecordRepository.save(record);
    }

    @Transactional
    public OvertimeRecord approve(Long id, Long approverId) {
        OvertimeRecord record = getById(id);
        if (record.getStatus() != OvertimeStatus.PENDING) {
            throw new IllegalStateException("Only PENDING records can be approved");
        }

        BigDecimal pay = calculateOvertimePay(record);
        record.approve(approverId, pay);
        return overtimeRecordRepository.save(record);
    }

    @Transactional
    public OvertimeRecord reject(Long id, Long approverId) {
        OvertimeRecord record = getById(id);
        record.reject(approverId);
        return overtimeRecordRepository.save(record);
    }

    BigDecimal calculateHours(LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    }

    BigDecimal calculateOvertimePay(OvertimeRecord record) {
        // Get employee's monthly salary
        SalaryStructure salary = salaryStructureRepository
                .findByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        record.getEmployee().getId(), record.getOvertimeDate())
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Salary structure not found for employee " + record.getEmployee().getId()));

        BigDecimal monthlySalary = salary.getBaseSalary();
        BigDecimal hourlyRate = monthlySalary.divide(HOURS_PER_MONTH, 2, RoundingMode.HALF_UP);
        BigDecimal hours = record.getHours();

        return switch (record.getOvertimeType()) {
            case WORKDAY -> calculateWorkdayOvertimePay(hourlyRate, hours);
            case REST_DAY -> calculateRestDayOvertimePay(hourlyRate, hours);
            case HOLIDAY -> calculateHolidayOvertimePay(hourlyRate, hours);
        };
    }

    private BigDecimal calculateWorkdayOvertimePay(BigDecimal hourlyRate, BigDecimal hours) {
        BigDecimal two = BigDecimal.valueOf(2);
        if (hours.compareTo(two) <= 0) {
            return hours.multiply(hourlyRate).multiply(RATE_WORKDAY_FIRST_2)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return two.multiply(hourlyRate).multiply(RATE_WORKDAY_FIRST_2)
                .add(hours.subtract(two).multiply(hourlyRate).multiply(RATE_WORKDAY_AFTER_2))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRestDayOvertimePay(BigDecimal hourlyRate, BigDecimal hours) {
        BigDecimal two = BigDecimal.valueOf(2);
        BigDecimal eight = BigDecimal.valueOf(8);
        if (hours.compareTo(two) <= 0) {
            return hours.multiply(hourlyRate).multiply(RATE_REST_FIRST_2)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        if (hours.compareTo(eight) <= 0) {
            return two.multiply(hourlyRate).multiply(RATE_REST_FIRST_2)
                    .add(hours.subtract(two).multiply(hourlyRate).multiply(RATE_REST_2_TO_8))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return two.multiply(hourlyRate).multiply(RATE_REST_FIRST_2)
                .add(BigDecimal.valueOf(6).multiply(hourlyRate).multiply(RATE_REST_2_TO_8))
                .add(hours.subtract(eight).multiply(hourlyRate).multiply(RATE_REST_OVER_8))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateHolidayOvertimePay(BigDecimal hourlyRate, BigDecimal hours) {
        return hours.multiply(hourlyRate).multiply(RATE_HOLIDAY)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
