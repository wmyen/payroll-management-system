package com.payroll.attendance.controller;

import com.payroll.attendance.domain.AttendanceRecord;
import com.payroll.attendance.service.AttendanceRecordService;
import com.payroll.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceRecordService attendanceRecordService;

    @GetMapping
    public ApiResponse<Page<AttendanceRecord>> search(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(attendanceRecordService.search(employeeId, departmentId, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<AttendanceRecord> getById(@PathVariable Long id) {
        return ApiResponse.ok(attendanceRecordService.getById(id));
    }

    @PostMapping("/import")
    public ApiResponse<List<AttendanceRecord>> importCsv(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(attendanceRecordService.importCsv(file), "Import completed");
    }
}
