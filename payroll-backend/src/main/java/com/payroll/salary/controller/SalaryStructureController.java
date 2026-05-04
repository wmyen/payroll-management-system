package com.payroll.salary.controller;

import com.payroll.salary.domain.SalaryStructure;
import com.payroll.salary.dto.SalaryStructureRequest;
import com.payroll.salary.service.SalaryStructureService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salary")
@RequiredArgsConstructor
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    @GetMapping
    public ApiResponse<SalaryStructure> getCurrent(@PathVariable Long employeeId) {
        return ApiResponse.ok(salaryStructureService.getCurrentByEmployeeId(employeeId));
    }

    @GetMapping("/history")
    public ApiResponse<List<SalaryStructure>> getHistory(@PathVariable Long employeeId) {
        return ApiResponse.ok(salaryStructureService.getHistoryByEmployeeId(employeeId));
    }

    @PostMapping
    public ApiResponse<SalaryStructure> create(
            @PathVariable Long employeeId,
            @Valid @RequestBody SalaryStructureRequest request) {
        request.setEmployeeId(employeeId);
        return ApiResponse.ok(salaryStructureService.create(request), "Salary structure created");
    }
}
