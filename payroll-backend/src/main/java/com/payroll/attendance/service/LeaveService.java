package com.payroll.attendance.service;

import com.payroll.attendance.domain.*;
import com.payroll.attendance.dto.LeaveRequestDto;
import com.payroll.attendance.repository.LeaveBalanceRepository;
import com.payroll.attendance.repository.LeaveRequestRepository;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    public Page<LeaveRequest> search(Long employeeId, LeaveType leaveType, LeaveRequestStatus status,
                                      LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return leaveRequestRepository.search(employeeId, leaveType, status, startDate, endDate, pageable);
    }

    public LeaveRequest getById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found: " + id));
    }

    @Transactional
    public LeaveRequest create(LeaveRequestDto request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + request.getEmployeeId()));

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(LeaveType.valueOf(request.getLeaveType()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startPeriod(request.getStartPeriod() != null ? LeavePeriod.valueOf(request.getStartPeriod()) : null)
                .endPeriod(request.getEndPeriod() != null ? LeavePeriod.valueOf(request.getEndPeriod()) : null)
                .daysCount(request.getDaysCount())
                .reason(request.getReason())
                .build();

        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest approve(Long id, Long approverId) {
        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }
        leaveRequest.approve(approverId);

        // Deduct leave balance
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndYear(leaveRequest.getEmployee().getId(),
                        leaveRequest.getLeaveType(), leaveRequest.getStartDate().getYear())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Leave balance not found for type " + leaveRequest.getLeaveType()));
        balance.useDays(leaveRequest.getDaysCount());

        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest reject(Long id, Long approverId, String reason) {
        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }
        leaveRequest.reject(approverId, reason);
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest cancel(Long id) {
        LeaveRequest leaveRequest = getById(id);
        leaveRequest.cancel();
        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveBalance> getBalances(Long employeeId, Integer year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
    }

    @Transactional
    public void initYearBalances(Integer year) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            initEmployeeBalances(emp, year);
        }
    }

    @Transactional
    public void initEmployeeBalances(Employee employee, Integer year) {
        int yearsOfService = Period.between(employee.getHireDate(), LocalDate.of(year, 1, 1)).getYears();

        // Annual leave per labor law §38
        BigDecimal annualDays = calculateAnnualLeaveDays(yearsOfService);

        createIfAbsent(employee, LeaveType.ANNUAL, year, annualDays);
        createIfAbsent(employee, LeaveType.SICK, year, new BigDecimal("30"));
        createIfAbsent(employee, LeaveType.PERSONAL, year, new BigDecimal("14"));
        createIfAbsent(employee, LeaveType.MARRIAGE, year, new BigDecimal("8"));
        createIfAbsent(employee, LeaveType.BEREAVEMENT, year, new BigDecimal("8"));
        createIfAbsent(employee, LeaveType.MATERNITY, year, new BigDecimal("56"));
        createIfAbsent(employee, LeaveType.PATERNITY, year, new BigDecimal("5"));
        createIfAbsent(employee, LeaveType.OFFICIAL, year, new BigDecimal("0"));
    }

    private BigDecimal calculateAnnualLeaveDays(int years) {
        if (years < 1) return new BigDecimal("3");
        if (years < 3) return new BigDecimal("7");
        if (years < 5) return new BigDecimal("10");
        if (years < 10) return new BigDecimal("14");
        int extra = Math.min(years - 10, 16);
        return new BigDecimal(String.valueOf(14 + extra));
    }

    private void createIfAbsent(Employee employee, LeaveType type, Integer year, BigDecimal totalDays) {
        leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(employee.getId(), type, year)
                .orElseGet(() -> leaveBalanceRepository.save(
                        LeaveBalance.builder()
                                .employee(employee)
                                .leaveType(type)
                                .year(year)
                                .totalDays(totalDays)
                                .build()));
    }

    @Transactional
    public LeaveBalance adjustBalance(Long employeeId, LeaveType leaveType, Integer year, BigDecimal newTotal) {
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndYear(employeeId, leaveType, year)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found"));
        balance.adjustTotalDays(newTotal);
        return leaveBalanceRepository.save(balance);
    }
}
