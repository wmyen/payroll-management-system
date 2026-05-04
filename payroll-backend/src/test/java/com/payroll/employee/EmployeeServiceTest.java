package com.payroll.employee;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import com.payroll.employee.dto.EmployeeRequest;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.employee.service.EmployeeService;
import com.payroll.shared.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void create_employee_encrypts_sensitive_fields() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("王大明");
        request.setIdNumber("A123456789");
        request.setBankAccount("1234567890");
        request.setHireDate(LocalDate.of(2024, 1, 1));
        request.setDepartmentId(null);

        when(encryptionUtil.encrypt("A123456789")).thenReturn("enc_id");
        when(encryptionUtil.encrypt("1234567890")).thenReturn("enc_bank");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.create(request);

        assertEquals("王大明", result.getName());
        verify(encryptionUtil).encrypt("A123456789");
        verify(encryptionUtil).encrypt("1234567890");
    }

    @Test
    void search_employees_with_pagination() {
        Page<Employee> page = new PageImpl<>(List.of(
                Employee.builder().name("王大明").build()
        ));
        when(employeeRepository.search("王", null, null, PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<Employee> result = employeeService.search("王", null, null, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void resign_employee_sets_status_and_leave_date() {
        Employee emp = Employee.builder()
                .name("王大明")
                .hireDate(LocalDate.of(2024, 1, 1))
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);

        LocalDate leaveDate = LocalDate.of(2024, 6, 30);
        employeeService.resign(1L, leaveDate);

        assertEquals(EmployeeStatus.LEFT, emp.getStatus());
        assertEquals(leaveDate, emp.getLeaveDate());
    }

    @Test
    void get_employee_decrypts_sensitive_fields() {
        Employee emp = Employee.builder()
                .name("王大明")
                .idNumber("enc_id")
                .bankAccount("enc_bank")
                .hireDate(LocalDate.of(2024, 1, 1))
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(encryptionUtil.decrypt("enc_id")).thenReturn("A123456789");
        when(encryptionUtil.decrypt("enc_bank")).thenReturn("1234567890");

        Employee result = employeeService.getById(1L);

        assertEquals("A123456789", result.getIdNumber());
        assertEquals("1234567890", result.getBankAccount());
    }
}
