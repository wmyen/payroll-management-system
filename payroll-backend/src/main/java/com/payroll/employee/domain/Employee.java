package com.payroll.employee.domain;

import com.payroll.department.domain.Department;
import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "emp_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String idNumber;

    @Column(length = 255)
    private String bankAccount;

    @Column(nullable = false)
    private LocalDate hireDate;

    private LocalDate leaveDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContractType contractType = ContractType.REGULAR;

    @Column(length = 50)
    private String jobLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    public void updateBasicInfo(String name, String email, String phone, String jobLevel) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.jobLevel = jobLevel;
    }

    public void assignToDepartment(Department department) {
        this.department = department;
    }

    public void resign(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
        this.status = EmployeeStatus.LEFT;
    }
}
