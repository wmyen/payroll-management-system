package com.payroll.payroll.repository;

import com.payroll.payroll.domain.TaxBracket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxBracketRepository extends JpaRepository<TaxBracket, Long> {
    List<TaxBracket> findByYearOrderByBracketStartAsc(int year);
}
