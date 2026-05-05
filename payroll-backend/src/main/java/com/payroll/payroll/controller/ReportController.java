package com.payroll.payroll.controller;

import com.payroll.payroll.service.ReportService;
import com.payroll.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/payroll-summary")
    public ApiResponse<Map<String, Object>> payrollSummary(@RequestParam Long periodId) {
        return ApiResponse.ok(reportService.getPayrollSummary(periodId));
    }

    @GetMapping("/department-cost")
    public ApiResponse<Map<String, Object>> departmentCost(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        return ApiResponse.ok(reportService.getDepartmentCost(year, month));
    }

    @GetMapping("/overtime-trend")
    public ApiResponse<Map<String, Object>> overtimeTrend(@RequestParam Integer year) {
        return ApiResponse.ok(reportService.getOvertimeTrend(year));
    }

    @GetMapping("/bank-transfer")
    public ResponseEntity<byte[]> bankTransfer(@RequestParam Long periodId) {
        String csv = reportService.generateBankTransfer(periodId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"bank-transfer.txt\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @GetMapping("/export/payroll")
    public ResponseEntity<byte[]> exportPayroll(@RequestParam Long periodId) throws Exception {
        byte[] excel = reportService.exportPayrollExcel(periodId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"payroll-report.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
