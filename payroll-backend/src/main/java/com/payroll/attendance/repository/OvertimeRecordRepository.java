package com.payroll.attendance.repository;

import com.payroll.attendance.domain.OvertimeRecord;
import com.payroll.attendance.domain.OvertimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OvertimeRecordRepository extends JpaRepository<OvertimeRecord, Long> {

    @Query("SELECT COALESCE(SUM(o.hours), 0) FROM OvertimeRecord o WHERE " +
           "o.employee.id = :employeeId AND o.overtimeDate BETWEEN :startDate AND :endDate " +
           "AND o.status = :status")
    BigDecimal sumApprovedHoursByEmployeeAndMonth(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("status") OvertimeStatus status);

    @Query("SELECT o FROM OvertimeRecord o WHERE " +
           "(:employeeId IS NULL OR o.employee.id = :employeeId) AND " +
           "(:departmentId IS NULL OR o.employee.department.id = :departmentId) AND " +
           "(:startDate IS NULL OR o.overtimeDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.overtimeDate <= :endDate)")
    org.springframework.data.domain.Page<OvertimeRecord> search(@Param("employeeId") Long employeeId,
                                                                 @Param("departmentId") Long departmentId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate,
                                                                 org.springframework.data.domain.Pageable pageable);
}
