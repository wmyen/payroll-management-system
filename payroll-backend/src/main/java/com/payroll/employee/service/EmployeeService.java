package com.payroll.employee.service;

import com.payroll.department.domain.Department;
import com.payroll.department.repository.DepartmentRepository;
import com.payroll.employee.domain.ContractType;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import com.payroll.employee.dto.EmployeeRequest;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.shared.util.EncryptionUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public Employee create(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .idNumber(encryptionUtil.encrypt(request.getIdNumber()))
                .hireDate(request.getHireDate())
                .contractType(request.getContractType() != null
                        ? ContractType.valueOf(request.getContractType()) : ContractType.REGULAR)
                .jobLevel(request.getJobLevel())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        if (request.getBankAccount() != null) {
            employee = Employee.builder()
                    .name(employee.getName())
                    .idNumber(employee.getIdNumber())
                    .hireDate(employee.getHireDate())
                    .contractType(employee.getContractType())
                    .jobLevel(employee.getJobLevel())
                    .email(employee.getEmail())
                    .phone(employee.getPhone())
                    .bankAccount(encryptionUtil.encrypt(request.getBankAccount()))
                    .build();
        }

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found: " + request.getDepartmentId()));
            employee.assignToDepartment(dept);
        }

        return employeeRepository.save(employee);
    }

    public Employee getById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        emp = decryptSensitiveFields(emp);
        return emp;
    }

    public Page<Employee> search(String name, Long departmentId, EmployeeStatus status, Pageable pageable) {
        return employeeRepository.search(name, departmentId, status, pageable);
    }

    @Transactional
    public Employee update(Long id, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        emp.updateBasicInfo(request.getName(), request.getEmail(), request.getPhone(), request.getJobLevel());
        return employeeRepository.save(emp);
    }

    @Transactional
    public void resign(Long id, LocalDate leaveDate) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        emp.resign(leaveDate);
        employeeRepository.save(emp);
    }

    private Employee decryptSensitiveFields(Employee emp) {
        Employee decrypted = Employee.builder()
                .hireDate(emp.getHireDate())
                .leaveDate(emp.getLeaveDate())
                .contractType(emp.getContractType())
                .jobLevel(emp.getJobLevel())
                .status(emp.getStatus())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .department(emp.getDepartment())
                .name(emp.getName())
                .idNumber(encryptionUtil.decrypt(emp.getIdNumber()))
                .bankAccount(emp.getBankAccount() != null ? encryptionUtil.decrypt(emp.getBankAccount()) : null)
                .build();
        return decrypted;
    }
}
