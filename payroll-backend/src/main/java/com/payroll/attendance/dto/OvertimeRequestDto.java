package com.payroll.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OvertimeRequestDto {
    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate overtimeDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private String overtimeType;
}
