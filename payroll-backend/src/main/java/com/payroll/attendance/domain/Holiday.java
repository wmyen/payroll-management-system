package com.payroll.attendance.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "att_holiday", uniqueConstraints = @UniqueConstraint(columnNames = "holiday_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Holiday extends BaseEntity {

    @Column(nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HolidayType holidayType;

    @Column(nullable = false)
    private Integer year;

    public void update(LocalDate holidayDate, String name, HolidayType holidayType, Integer year) {
        this.holidayDate = holidayDate;
        this.name = name;
        this.holidayType = holidayType;
        this.year = year;
    }
}
