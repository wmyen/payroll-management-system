package com.payroll.employee.controller;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import com.payroll.employee.dto.EmployeeRequest;
import com.payroll.employee.service.EmployeeService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ApiResponse<Page<Employee>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(employeeService.search(name, departmentId, status, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<Employee> getById(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.getById(id));
    }

    @PostMapping
    public ApiResponse<Employee> create(@Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.ok(employeeService.create(request), "Employee created");
    }

    @PutMapping("/{id}")
    public ApiResponse<Employee> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.ok(employeeService.update(id, request), "Employee updated");
    }

    @PutMapping("/{id}/resign")
    public ApiResponse<Void> resign(@PathVariable Long id, @RequestParam LocalDate leaveDate) {
        employeeService.resign(id, leaveDate);
        return ApiResponse.ok(null, "Employee resigned");
    }
}
