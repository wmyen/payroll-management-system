package com.payroll.salary.domain;

import com.payroll.employee.domain.Employee;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emp_salary_structure")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SalaryStructure extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @OneToMany(mappedBy = "salaryStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Allowance> allowances = new ArrayList<>();

    public BigDecimal getTotalAllowances() {
        return allowances.stream()
                .map(Allowance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrossSalary() {
        return baseSalary.add(getTotalAllowances());
    }

    public void addAllowance(Allowance allowance) {
        allowances.add(allowance);
        allowance.setSalaryStructure(this);
    }

    public void removeAllowance(Allowance allowance) {
        allowances.remove(allowance);
        allowance.setSalaryStructure(null);
    }
}
