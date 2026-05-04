package com.payroll.attendance.repository;

import com.payroll.attendance.domain.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByEmployeeIdAndRecordDate(Long employeeId, LocalDate recordDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE " +
           "(:employeeId IS NULL OR a.employee.id = :employeeId) AND " +
           "(:departmentId IS NULL OR a.employee.department.id = :departmentId) AND " +
           "(:startDate IS NULL OR a.recordDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.recordDate <= :endDate)")
    Page<AttendanceRecord> search(@Param("employeeId") Long employeeId,
                                  @Param("departmentId") Long departmentId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  Pageable pageable);
}
