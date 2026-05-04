package com.payroll.payroll.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayrollItemRequest {
    private String itemType;
    private String name;
    private BigDecimal amount;
    private String remark;
}
