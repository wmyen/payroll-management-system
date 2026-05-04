package com.payroll.payroll.controller;

import com.payroll.payroll.domain.TaxBracket;
import com.payroll.payroll.dto.TaxBracketRequest;
import com.payroll.payroll.service.TaxBracketService;
import com.payroll.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll/tax-brackets")
@RequiredArgsConstructor
public class TaxBracketController {

    private final TaxBracketService taxBracketService;

    @GetMapping
    public ApiResponse<List<TaxBracket>> getByYear(@RequestParam int year) {
        return ApiResponse.ok(taxBracketService.getByYear(year));
    }

    @PostMapping
    public ApiResponse<List<TaxBracket>> createBrackets(@RequestBody TaxBracketRequest request) {
        return ApiResponse.ok(taxBracketService.createBrackets(request), "Tax brackets saved");
    }
}
