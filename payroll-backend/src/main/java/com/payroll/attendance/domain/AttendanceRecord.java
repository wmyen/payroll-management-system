package com.payroll.attendance.domain;

import com.payroll.employee.domain.Employee;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "att_record", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "record_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate recordDate;

    private LocalTime clockIn;

    private LocalTime clockOut;

    @Column(precision = 4, scale = 1)
    private BigDecimal workHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.NORMAL;

    @Column(length = 200)
    private String remark;

    public void updateClockRecord(LocalTime clockIn, LocalTime clockOut, BigDecimal workHours, AttendanceStatus status) {
        this.clockIn = clockIn;
        this.clockOut = clockOut;
        this.workHours = workHours;
        this.status = status;
    }

    public void updateStatus(AttendanceStatus status) {
        this.status = status;
    }
}
