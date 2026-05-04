package com.payroll.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    private String bankAccount;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    private Long departmentId;

    private String contractType;

    private String jobLevel;

    private String email;

    private String phone;
}
