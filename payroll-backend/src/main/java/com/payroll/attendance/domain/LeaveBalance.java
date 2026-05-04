package com.payroll.attendance.domain;

import com.payroll.employee.domain.Employee;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "att_leave_balance", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type", "year"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal totalDays;

    @Column(nullable = false, precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal usedDays = BigDecimal.ZERO;

    public BigDecimal getRemainingDays() {
        return totalDays.subtract(usedDays);
    }

    public void useDays(BigDecimal days) {
        BigDecimal newUsed = this.usedDays.add(days);
        if (newUsed.compareTo(totalDays) > 0) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }
        this.usedDays = newUsed;
    }

    public void adjustTotalDays(BigDecimal newTotal) {
        this.totalDays = newTotal;
    }
}
