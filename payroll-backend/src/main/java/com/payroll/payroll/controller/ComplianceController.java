package com.payroll.payroll.controller;

import com.payroll.payroll.domain.InsuranceRate;
import com.payroll.payroll.domain.WithholdingStatement;
import com.payroll.payroll.dto.InsuranceRateRequest;
import com.payroll.payroll.service.InsuranceRateService;
import com.payroll.payroll.service.WithholdingService;
import com.payroll.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final InsuranceRateService insuranceRateService;
    private final WithholdingService withholdingService;

    // === Insurance Rates ===

    @GetMapping("/insurance-rates")
    public ApiResponse<List<InsuranceRate>> listInsuranceRates() {
        return ApiResponse.ok(insuranceRateService.getAll());
    }

    @GetMapping("/insurance-rates/{id}")
    public ApiResponse<InsuranceRate> getInsuranceRate(@PathVariable Long id) {
        return ApiResponse.ok(insuranceRateService.getById(id));
    }

    @PostMapping("/insurance-rates")
    public ApiResponse<InsuranceRate> createInsuranceRate(@RequestBody InsuranceRateRequest request) {
        return ApiResponse.ok(insuranceRateService.create(request), "Insurance rate version created");
    }

    // === Withholding Statements ===

    @GetMapping("/withholding")
    public ApiResponse<List<WithholdingStatement>> listWithholding(@RequestParam int year) {
        return ApiResponse.ok(withholdingService.getByYear(year));
    }

    @PostMapping("/withholding/generate")
    public ApiResponse<List<WithholdingStatement>> generateWithholding(@RequestBody Map<String, Integer> body) {
        return ApiResponse.ok(withholdingService.generateForYear(body.get("year")), "Withholding statements generated");
    }

    @GetMapping("/withholding/{id}")
    public ApiResponse<Map<String, Object>> getWithholding(@PathVariable Long id) {
        return ApiResponse.ok(withholdingService.getById(id));
    }

    @PostMapping("/withholding/{id}/confirm")
    public ApiResponse<Void> confirmWithholding(@PathVariable Long id) {
        withholdingService.confirm(id);
        return ApiResponse.ok(null, "Withholding statement confirmed");
    }

    @PostMapping("/withholding/confirm-all")
    public ApiResponse<Void> confirmAllWithholding(@RequestBody Map<String, Integer> body) {
        withholdingService.confirmAll(body.get("year"));
        return ApiResponse.ok(null, "All withholding statements confirmed");
    }
}
