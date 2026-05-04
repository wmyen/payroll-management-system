package com.payroll.attendance.controller;

import com.payroll.attendance.domain.OvertimeRecord;
import com.payroll.attendance.dto.OvertimeRequestDto;
import com.payroll.attendance.service.OvertimeService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    @GetMapping
    public ApiResponse<Page<OvertimeRecord>> list(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(overtimeService.search(employeeId, departmentId, startDate, endDate, pageable));
    }

    @PostMapping
    public ApiResponse<OvertimeRecord> create(@Valid @RequestBody OvertimeRequestDto request) {
        return ApiResponse.ok(overtimeService.create(request), "Overtime record created");
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<OvertimeRecord> approve(@PathVariable Long id, @RequestParam Long approverId) {
        return ApiResponse.ok(overtimeService.approve(id, approverId), "Overtime approved");
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<OvertimeRecord> reject(@PathVariable Long id, @RequestParam Long approverId) {
        return ApiResponse.ok(overtimeService.reject(id, approverId), "Overtime rejected");
    }
}
