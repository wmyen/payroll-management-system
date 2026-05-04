package com.payroll.payroll.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayrollRecordUpdateRequest {
    private BigDecimal otherEarnings;
    private BigDecimal otherDeductions;
    private String remark;
}
