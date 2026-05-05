package com.payroll.payroll.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InsuranceRateRequest {
    private LocalDate effectiveDate;
    private String description;
    private BigDecimal laborRate;
    private BigDecimal employmentInsuranceRate;
    private BigDecimal occupationalRate;
    private BigDecimal employeeLaborShare;
    private BigDecimal employerLaborShare;
    private BigDecimal healthRate;
    private BigDecimal healthEmployeeShare;
    private BigDecimal healthEmployerShare;
    private BigDecimal pensionRate;
}
