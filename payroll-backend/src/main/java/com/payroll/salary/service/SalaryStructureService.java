package com.payroll.salary.service;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.salary.domain.Allowance;
import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.dto.SalaryStructureRequest;
import com.payroll.salary.repository.SalaryStructureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public SalaryStructure create(SalaryStructureRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + request.getEmployeeId()));

        SalaryStructure ss = SalaryStructure.builder()
                .employee(emp)
                .baseSalary(request.getBaseSalary())
                .effectiveDate(request.getEffectiveDate())
                .build();

        if (request.getAllowances() != null) {
            for (SalaryStructureRequest.AllowanceDto dto : request.getAllowances()) {
                Allowance allowance = Allowance.builder()
                        .type(dto.getType())
                        .amount(dto.getAmount())
                        .build();
                ss.addAllowance(allowance);
            }
        }

        return salaryStructureRepository.save(ss);
    }

    public SalaryStructure getCurrentByEmployeeId(Long employeeId) {
        return salaryStructureRepository.findFirstByEmployeeIdOrderByEffectiveDateDesc(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("No salary structure found for employee: " + employeeId));
    }

    public List<SalaryStructure> getHistoryByEmployeeId(Long employeeId) {
        return salaryStructureRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }
}
