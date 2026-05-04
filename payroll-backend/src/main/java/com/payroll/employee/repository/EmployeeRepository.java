package com.payroll.employee.repository;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.domain.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR e.name LIKE %:name%) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Employee> search(@Param("name") String name,
                          @Param("departmentId") Long departmentId,
                          @Param("status") EmployeeStatus status,
                          Pageable pageable);

    List<Employee> findByDepartmentId(Long departmentId);
}
