package com.payroll.attendance.repository;

import com.payroll.attendance.domain.LeaveRequest;
import com.payroll.attendance.domain.LeaveRequestStatus;
import com.payroll.attendance.domain.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("SELECT l FROM LeaveRequest l WHERE " +
           "(:employeeId IS NULL OR l.employee.id = :employeeId) AND " +
           "(:leaveType IS NULL OR l.leaveType = :leaveType) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:startDate IS NULL OR l.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR l.endDate <= :endDate)")
    Page<LeaveRequest> search(@Param("employeeId") Long employeeId,
                              @Param("leaveType") LeaveType leaveType,
                              @Param("status") LeaveRequestStatus status,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              Pageable pageable);

    @Query("SELECT l FROM LeaveRequest l WHERE " +
           "l.employee.id = :employeeId AND l.status = 'APPROVED' AND " +
           "l.startDate <= :endDate AND l.endDate >= :startDate")
    List<LeaveRequest> findApprovedInRange(@Param("employeeId") Long employeeId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
