package com.payroll.payroll.controller;

import com.payroll.payroll.service.EssService;
import com.payroll.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ess")
@RequiredArgsConstructor
public class EssController {

    private final EssService essService;

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.ok(essService.getCurrentUser());
    }

    @GetMapping("/paystubs")
    public ApiResponse<List<Map<String, Object>>> paystubs() {
        return ApiResponse.ok(essService.getMyPaystubs());
    }

    @GetMapping("/paystubs/{recordId}")
    public ApiResponse<Map<String, Object>> paystubDetail(@PathVariable Long recordId) {
        return ApiResponse.ok(essService.getPaystubDetail(recordId));
    }
}
