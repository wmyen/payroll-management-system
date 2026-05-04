package com.payroll.payroll.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PayrollPeriodRequest {
    private int year;
    private int month;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate payDate;
}
