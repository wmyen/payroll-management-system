package com.payroll.attendance.domain;

import com.payroll.employee.domain.Employee;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "att_leave_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private LeavePeriod startPeriod;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private LeavePeriod endPeriod;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal daysCount;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    @Column(name = "approver_id")
    private Long approverId;

    private LocalDateTime approvedAt;

    @Column(length = 200)
    private String rejectedReason;

    public void approve(Long approverId) {
        this.approverId = approverId;
        this.approvedAt = LocalDateTime.now();
        this.status = LeaveRequestStatus.APPROVED;
    }

    public void reject(Long approverId, String reason) {
        this.approverId = approverId;
        this.approvedAt = LocalDateTime.now();
        this.rejectedReason = reason;
        this.status = LeaveRequestStatus.REJECTED;
    }

    public void cancel() {
        this.status = LeaveRequestStatus.CANCELLED;
    }
}
