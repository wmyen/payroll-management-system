package com.payroll.payroll.controller;

import com.payroll.employee.domain.Employee;
import com.payroll.payroll.domain.*;
import com.payroll.payroll.dto.PayrollItemRequest;
import com.payroll.payroll.dto.PayrollPeriodRequest;
import com.payroll.payroll.dto.PayrollRecordUpdateRequest;
import com.payroll.payroll.service.PayrollPeriodService;
import com.payroll.payroll.service.PayrollRecordService;
import com.payroll.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollPeriodService periodService;
    private final PayrollRecordService recordService;

    // === Period ===

    @GetMapping("/periods")
    public ApiResponse<List<PayrollPeriod>> listPeriods() {
        return ApiResponse.ok(periodService.getAll());
    }

    @PostMapping("/periods")
    public ApiResponse<PayrollPeriod> createPeriod(@RequestBody PayrollPeriodRequest request) {
        return ApiResponse.ok(periodService.create(request));
    }

    @PutMapping("/periods/{id}")
    public ApiResponse<PayrollPeriod> updatePeriod(@PathVariable Long id,
                                                    @RequestBody PayrollPeriodRequest request) {
        return ApiResponse.ok(periodService.update(id, request));
    }

    @PostMapping("/periods/{id}/calculate")
    public ApiResponse<List<PayrollRecord>> calculate(@PathVariable Long id) {
        return ApiResponse.ok(periodService.calculate(id), "Calculation completed");
    }

    @PostMapping("/periods/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        periodService.confirm(id);
        return ApiResponse.ok(null, "Period locked");
    }

    // === Records ===

    @GetMapping("/records")
    public ApiResponse<List<PayrollRecord>> listRecords(@RequestParam Long periodId) {
        return ApiResponse.ok(recordService.getByPeriodId(periodId));
    }

    @GetMapping("/records/{id}")
    public ApiResponse<Map<String, Object>> getRecord(@PathVariable Long id) {
        PayrollRecord record = recordService.getById(id);
        Employee employee = recordService.getEmployeeForRecord(id);
        List<PayrollItem> items = recordService.getItems(id);
        Map<String, Object> empInfo = new java.util.HashMap<>();
        empInfo.put("id", employee.getId());
        empInfo.put("name", employee.getName());
        empInfo.put("department", employee.getDepartment() != null
                ? Map.of("name", employee.getDepartment().getName()) : null);
        return ApiResponse.ok(Map.of("record", record, "employee", empInfo, "items", items));
    }

    @PutMapping("/records/{id}")
    public ApiResponse<PayrollRecord> updateRecord(@PathVariable Long id,
                                                    @RequestBody PayrollRecordUpdateRequest request) {
        return ApiResponse.ok(recordService.update(id, request));
    }

    // === Items ===

    @GetMapping("/records/{recordId}/items")
    public ApiResponse<List<PayrollItem>> listItems(@PathVariable Long recordId) {
        return ApiResponse.ok(recordService.getItems(recordId));
    }

    @PostMapping("/records/{recordId}/items")
    public ApiResponse<PayrollItem> addItem(@PathVariable Long recordId,
                                             @RequestBody PayrollItemRequest request) {
        return ApiResponse.ok(recordService.addItem(recordId, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        recordService.deleteItem(id);
        return ApiResponse.ok(null, "Item deleted");
    }
}
