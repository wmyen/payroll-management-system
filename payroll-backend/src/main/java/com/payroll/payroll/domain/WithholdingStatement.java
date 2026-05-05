package com.payroll.payroll.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pay_withholding_statement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WithholdingStatement extends BaseEntity {

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private Long employeeId;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalLaborInsurance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalHealthInsurance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalIncomeTax = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalNetPay = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalEmployerCost = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private int monthCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WithholdingStatus status = WithholdingStatus.DRAFT;

    public void confirm() {
        this.status = WithholdingStatus.CONFIRMED;
    }

    public void updateTotals(BigDecimal totalGross, BigDecimal totalLaborInsurance,
                             BigDecimal totalHealthInsurance, BigDecimal totalIncomeTax,
                             BigDecimal totalNetPay, BigDecimal totalEmployerCost,
                             int monthCount) {
        this.totalGross = totalGross;
        this.totalLaborInsurance = totalLaborInsurance;
        this.totalHealthInsurance = totalHealthInsurance;
        this.totalIncomeTax = totalIncomeTax;
        this.totalNetPay = totalNetPay;
        this.totalEmployerCost = totalEmployerCost;
        this.monthCount = monthCount;
    }
}
