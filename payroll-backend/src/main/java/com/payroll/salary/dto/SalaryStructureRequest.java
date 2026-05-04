package com.payroll.salary.dto;

import com.payroll.salary.domain.AllowanceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SalaryStructureRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Base salary is required")
    private BigDecimal baseSalary;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private List<AllowanceDto> allowances;

    @Getter
    @Setter
    public static class AllowanceDto {
        private AllowanceType type;
        private BigDecimal amount;
    }
}
