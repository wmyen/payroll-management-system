package com.payroll.attendance.controller;

import com.payroll.attendance.domain.Holiday;
import com.payroll.attendance.dto.HolidayRequest;
import com.payroll.attendance.service.HolidayService;
import com.payroll.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public ApiResponse<List<Holiday>> list(@RequestParam Integer year) {
        return ApiResponse.ok(holidayService.findByYear(year));
    }

    @GetMapping("/{id}")
    public ApiResponse<Holiday> getById(@PathVariable Long id) {
        return ApiResponse.ok(holidayService.getById(id));
    }

    @PostMapping
    public ApiResponse<Holiday> create(@Valid @RequestBody HolidayRequest request) {
        return ApiResponse.ok(holidayService.create(request), "Holiday created");
    }

    @PutMapping("/{id}")
    public ApiResponse<Holiday> update(@PathVariable Long id, @Valid @RequestBody HolidayRequest request) {
        return ApiResponse.ok(holidayService.update(id, request), "Holiday updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        holidayService.delete(id);
        return ApiResponse.ok(null, "Holiday deleted");
    }
}
