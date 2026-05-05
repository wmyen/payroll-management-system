package com.payroll.payroll.repository;

import com.payroll.payroll.domain.WithholdingStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WithholdingStatementRepository extends JpaRepository<WithholdingStatement, Long> {

    List<WithholdingStatement> findByYearOrderByEmployeeId(int year);

    Optional<WithholdingStatement> findByYearAndEmployeeId(int year, Long employeeId);

    List<WithholdingStatement> findByYearAndStatus(int year, com.payroll.payroll.domain.WithholdingStatus status);
}
