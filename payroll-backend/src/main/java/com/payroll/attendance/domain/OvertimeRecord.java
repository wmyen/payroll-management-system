package com.payroll.attendance.domain;

import com.payroll.employee.domain.Employee;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "att_overtime_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OvertimeRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate overtimeDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal hours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OvertimeType overtimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OvertimeStatus status = OvertimeStatus.PENDING;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(precision = 15, scale = 2)
    private BigDecimal overtimePay;

    public void approve(Long approverId, BigDecimal overtimePay) {
        this.approverId = approverId;
        this.overtimePay = overtimePay;
        this.status = OvertimeStatus.APPROVED;
    }

    public void reject(Long approverId) {
        this.approverId = approverId;
        this.status = OvertimeStatus.REJECTED;
    }
}
