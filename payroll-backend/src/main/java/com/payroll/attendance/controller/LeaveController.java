package com.payroll.attendance.controller;

import com.payroll.attendance.domain.LeaveBalance;
import com.payroll.attendance.domain.LeaveRequest;
import com.payroll.attendance.domain.LeaveRequestStatus;
import com.payroll.attendance.domain.LeaveType;
import com.payroll.attendance.dto.LeaveRequestDto;
import com.payroll.attendance.service.LeaveService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping
    public ApiResponse<Page<LeaveRequest>> list(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveType leaveType,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(leaveService.search(employeeId, leaveType, status, startDate, endDate, pageable));
    }

    @PostMapping
    public ApiResponse<LeaveRequest> create(@Valid @RequestBody LeaveRequestDto request) {
        return ApiResponse.ok(leaveService.create(request), "Leave request created");
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<LeaveRequest> approve(@PathVariable Long id, @RequestParam Long approverId) {
        return ApiResponse.ok(leaveService.approve(id, approverId), "Leave request approved");
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<LeaveRequest> reject(@PathVariable Long id,
                                            @RequestParam Long approverId,
                                            @RequestBody java.util.Map<String, String> body) {
        return ApiResponse.ok(leaveService.reject(id, approverId, body.get("reason")), "Leave request rejected");
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<LeaveRequest> cancel(@PathVariable Long id) {
        return ApiResponse.ok(leaveService.cancel(id), "Leave request cancelled");
    }

    @GetMapping("/balances")
    public ApiResponse<List<LeaveBalance>> balances(
            @RequestParam Long employeeId, @RequestParam Integer year) {
        return ApiResponse.ok(leaveService.getBalances(employeeId, year));
    }

    @PostMapping("/balances/init")
    public ApiResponse<Void> initBalances(@RequestParam Integer year) {
        leaveService.initYearBalances(year);
        return ApiResponse.ok(null, "Leave balances initialized for year " + year);
    }
}
