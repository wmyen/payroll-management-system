package com.payroll.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayRequest {
    @NotNull
    private LocalDate holidayDate;

    @NotBlank
    private String name;

    @NotBlank
    private String holidayType;

    @NotNull
    private Integer year;
}
