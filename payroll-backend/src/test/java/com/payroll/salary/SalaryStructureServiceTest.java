package com.payroll.salary;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.salary.domain.Allowance;
import com.payroll.salary.domain.AllowanceType;
import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.dto.SalaryStructureRequest;
import com.payroll.salary.repository.SalaryStructureRepository;
import com.payroll.salary.service.SalaryStructureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryStructureServiceTest {

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SalaryStructureService salaryStructureService;

    @Test
    void create_salary_structure_with_allowances() {
        Employee emp = Employee.builder().name("王大明").hireDate(LocalDate.of(2024,1,1)).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(inv -> inv.getArgument(0));

        SalaryStructureRequest.AllowanceDto allowance = new SalaryStructureRequest.AllowanceDto();
        allowance.setType(AllowanceType.TRANSPORT);
        allowance.setAmount(new BigDecimal("3000"));

        SalaryStructureRequest request = new SalaryStructureRequest();
        request.setEmployeeId(1L);
        request.setBaseSalary(new BigDecimal("50000"));
        request.setEffectiveDate(LocalDate.of(2024, 1, 1));
        request.setAllowances(List.of(allowance));

        SalaryStructure result = salaryStructureService.create(request);

        assertEquals(new BigDecimal("50000"), result.getBaseSalary());
        assertEquals(new BigDecimal("53000"), result.getGrossSalary());
    }

    @Test
    void get_current_salary_structure() {
        SalaryStructure ss = SalaryStructure.builder()
                .baseSalary(new BigDecimal("50000"))
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();
        when(salaryStructureRepository.findFirstByEmployeeIdOrderByEffectiveDateDesc(1L))
                .thenReturn(Optional.of(ss));

        SalaryStructure result = salaryStructureService.getCurrentByEmployeeId(1L);

        assertEquals(new BigDecimal("50000"), result.getBaseSalary());
    }
}
