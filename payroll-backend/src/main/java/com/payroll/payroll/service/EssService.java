package com.payroll.payroll.service;

import com.payroll.auth.domain.User;
import com.payroll.auth.repository.UserRepository;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.payroll.domain.PayrollItem;
import com.payroll.payroll.domain.PayrollPeriod;
import com.payroll.payroll.domain.PayrollRecord;
import com.payroll.payroll.repository.PayrollItemRepository;
import com.payroll.payroll.repository.PayrollPeriodRepository;
import com.payroll.payroll.repository.PayrollRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EssService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollItemRepository payrollItemRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUser() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", user.getUsername());
        result.put("role", user.getRole().name());
        result.put("employeeId", user.getEmployeeId());

        if (user.getEmployeeId() != null) {
            Employee emp = employeeRepository.findById(user.getEmployeeId()).orElse(null);
            if (emp != null) {
                Map<String, Object> profile = new LinkedHashMap<>();
                profile.put("id", emp.getId());
                profile.put("name", emp.getName());
                profile.put("email", emp.getEmail());
                profile.put("phone", emp.getPhone());
                profile.put("hireDate", emp.getHireDate());
                profile.put("contractType", emp.getContractType());
                profile.put("jobLevel", emp.getJobLevel());
                profile.put("status", emp.getStatus());
                if (emp.getDepartment() != null) {
                    profile.put("department", Map.of("id", emp.getDepartment().getId(), "name", emp.getDepartment().getName()));
                }
                result.put("profile", profile);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyPaystubs() {
        Long employeeId = resolveEmployeeId();
        List<PayrollRecord> records = payrollRecordRepository.findByEmployeeIdOrderByPeriodIdDesc(employeeId);

        return records.stream().map(record -> {
            PayrollPeriod period = payrollPeriodRepository.findById(record.getPeriodId()).orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordId", record.getId());
            item.put("period", period != null ? Map.of(
                    "id", period.getId(),
                    "year", period.getYear(),
                    "month", period.getMonth(),
                    "payDate", period.getPayDate(),
                    "status", period.getStatus()
            ) : null);
            item.put("baseSalary", record.getBaseSalary());
            item.put("totalAllowances", record.getTotalAllowances());
            item.put("overtimePay", record.getOvertimePay());
            item.put("otherEarnings", record.getOtherEarnings());
            item.put("grossPay", record.getGrossPay());
            item.put("laborInsurance", record.getLaborInsurance());
            item.put("healthInsurance", record.getHealthInsurance());
            item.put("incomeTax", record.getIncomeTax());
            item.put("leaveDeduction", record.getLeaveDeduction());
            item.put("otherDeductions", record.getOtherDeductions());
            item.put("totalDeductions", record.getTotalDeductions());
            item.put("netPay", record.getNetPay());
            item.put("status", record.getStatus());
            return item;
        }).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaystubDetail(Long recordId) {
        Long employeeId = resolveEmployeeId();
        PayrollRecord record = payrollRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Payroll record not found"));

        if (!record.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Access denied");
        }

        PayrollPeriod period = payrollPeriodRepository.findById(record.getPeriodId()).orElse(null);
        List<PayrollItem> items = payrollItemRepository.findByPayrollRecordId(recordId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("record", record);
        result.put("period", period != null ? Map.of(
                "id", period.getId(),
                "year", period.getYear(),
                "month", period.getMonth(),
                "startDate", period.getStartDate(),
                "endDate", period.getEndDate(),
                "payDate", period.getPayDate()
        ) : null);
        result.put("items", items);
        return result;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("Not authenticated");
        return auth.getName();
    }

    private Long resolveEmployeeId() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getEmployeeId() == null) {
            throw new IllegalStateException("User has no associated employee record");
        }
        return user.getEmployeeId();
    }
}
