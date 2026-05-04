package com.payroll.department.service;

import com.payroll.department.domain.Department;
import com.payroll.department.dto.DepartmentRequest;
import com.payroll.department.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public Department create(DepartmentRequest request) {
        Department.DepartmentBuilder builder = Department.builder().name(request.getName());
        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent department not found: " + request.getParentId()));
            builder.parent(parent);
        }
        return departmentRepository.save(builder.build());
    }

    @Transactional
    public Department update(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + id));
        dept.updateName(request.getName());
        return departmentRepository.save(dept);
    }

    public List<Department> getRootDepartments() {
        return departmentRepository.findByParentIsNull();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        Department dept = getById(id);
        if (!dept.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete department with child departments");
        }
        departmentRepository.delete(dept);
    }
}
