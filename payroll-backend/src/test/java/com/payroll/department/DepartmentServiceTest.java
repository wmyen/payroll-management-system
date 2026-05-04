package com.payroll.department;

import com.payroll.department.domain.Department;
import com.payroll.department.dto.DepartmentRequest;
import com.payroll.department.repository.DepartmentRepository;
import com.payroll.department.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void create_department_without_parent() {
        DepartmentRequest request = new DepartmentRequest("Engineering", null);
        Department saved = Department.builder().name("Engineering").build();
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        Department result = departmentService.create(request);

        assertEquals("Engineering", result.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void create_department_with_parent() {
        Department parent = Department.builder().name("Company").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentRequest request = new DepartmentRequest("Engineering", 1L);
        Department result = departmentService.create(request);

        assertEquals("Engineering", result.getName());
        assertEquals(parent, result.getParent());
    }

    @Test
    void create_department_with_nonexistent_parent_throws() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        DepartmentRequest request = new DepartmentRequest("Engineering", 999L);
        assertThrows(IllegalArgumentException.class, () -> departmentService.create(request));
    }

    @Test
    void get_tree_returns_root_departments() {
        Department root = Department.builder().name("Company").build();
        when(departmentRepository.findByParentIsNull()).thenReturn(List.of(root));
        List<Department> roots = departmentService.getRootDepartments();
        assertEquals(1, roots.size());
        assertEquals("Company", roots.get(0).getName());
    }

    @Test
    void update_department_name() {
        Department dept = Department.builder().name("Old Name").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(departmentRepository.save(any(Department.class))).thenReturn(dept);
        Department result = departmentService.update(1L, new DepartmentRequest("New Name", null));
        assertEquals("New Name", result.getName());
    }
}
