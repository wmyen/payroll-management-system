package com.payroll.payroll.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "pay_payroll_period")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PayrollPeriod extends BaseEntity {

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate payDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PayrollPeriodStatus status = PayrollPeriodStatus.DRAFT;

    public void update(LocalDate startDate, LocalDate endDate, LocalDate payDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.payDate = payDate;
    }

    public void markProcessing() {
        this.status = PayrollPeriodStatus.PROCESSING;
    }

    public void confirm() {
        this.status = PayrollPeriodStatus.CONFIRMED;
    }

    public void lock() {
        this.status = PayrollPeriodStatus.LOCKED;
    }
}
