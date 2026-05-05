package com.payroll.payroll.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pay_insurance_rate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InsuranceRate extends BaseEntity {

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(length = 200)
    private String description;

    // 勞保
    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal laborRate = new BigDecimal("0.1100");

    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal employmentInsuranceRate = new BigDecimal("0.0100");

    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal occupationalRate = new BigDecimal("0.0020");

    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal employeeLaborShare = new BigDecimal("0.2000");

    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal employerLaborShare = new BigDecimal("0.7000");

    // 健保
    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal healthRate = new BigDecimal("0.0517");

    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal healthEmployeeShare = new BigDecimal("0.3000");

    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal healthEmployerShare = new BigDecimal("0.6000");

    // 勞退
    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal pensionRate = new BigDecimal("0.0600");
}
