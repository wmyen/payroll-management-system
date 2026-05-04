package com.payroll.department.repository;

import com.payroll.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByParentIsNull();
    List<Department> findByParentId(Long parentId);
    boolean existsByName(String name);
}
