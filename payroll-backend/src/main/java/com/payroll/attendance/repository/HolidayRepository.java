package com.payroll.attendance.repository;

import com.payroll.attendance.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByYearOrderByHolidayDate(Integer year);
    Optional<Holiday> findByHolidayDate(LocalDate holidayDate);
    boolean existsByHolidayDate(LocalDate holidayDate);
}
