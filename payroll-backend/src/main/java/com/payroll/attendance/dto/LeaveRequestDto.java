package com.payroll.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LeaveRequestDto {
    @NotNull
    private Long employeeId;

    @NotNull
    private String leaveType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String startPeriod;
    private String endPeriod;

    @NotNull
    private BigDecimal daysCount;

    private String reason;
}
