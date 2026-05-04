package com.payroll.payroll.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pay_payroll_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PayrollRecord extends BaseEntity {

    @Column(nullable = false)
    private Long periodId;

    @Column(nullable = false)
    private Long employeeId;

    // === Earnings ===
    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalAllowances = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal otherEarnings = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grossPay = BigDecimal.ZERO;

    // === Deductions ===
    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal laborInsurance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal healthInsurance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal incomeTax = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal leaveDeduction = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    // === Net ===
    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal netPay = BigDecimal.ZERO;

    // === Employer costs ===
    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal employerLaborIns = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal employerHealthIns = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal employerPension = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalEmployerCost = BigDecimal.ZERO;

    // === Status ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PayrollRecordStatus status = PayrollRecordStatus.DRAFT;

    @Column(length = 500)
    private String remark;

    public void confirm() {
        this.status = PayrollRecordStatus.CONFIRMED;
    }

    public void updateRemark(String remark) {
        this.remark = remark;
    }

    public void setCalculationResult(BigDecimal baseSalary, BigDecimal totalAllowances,
                                      BigDecimal overtimePay, BigDecimal otherEarnings,
                                      BigDecimal grossPay, BigDecimal laborInsurance,
                                      BigDecimal healthInsurance, BigDecimal incomeTax,
                                      BigDecimal leaveDeduction, BigDecimal otherDeductions,
                                      BigDecimal totalDeductions, BigDecimal netPay,
                                      BigDecimal employerLaborIns, BigDecimal employerHealthIns,
                                      BigDecimal employerPension, BigDecimal totalEmployerCost) {
        this.baseSalary = baseSalary;
        this.totalAllowances = totalAllowances;
        this.overtimePay = overtimePay;
        this.otherEarnings = otherEarnings;
        this.grossPay = grossPay;
        this.laborInsurance = laborInsurance;
        this.healthInsurance = healthInsurance;
        this.incomeTax = incomeTax;
        this.leaveDeduction = leaveDeduction;
        this.otherDeductions = otherDeductions;
        this.totalDeductions = totalDeductions;
        this.netPay = netPay;
        this.employerLaborIns = employerLaborIns;
        this.employerHealthIns = employerHealthIns;
        this.employerPension = employerPension;
        this.totalEmployerCost = totalEmployerCost;
    }
}
