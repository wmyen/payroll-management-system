package com.payroll.payroll.service;

import com.payroll.payroll.domain.TaxBracket;
import com.payroll.payroll.dto.TaxBracketRequest;
import com.payroll.payroll.repository.TaxBracketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxBracketService {

    private final TaxBracketRepository taxBracketRepository;

    public List<TaxBracket> getByYear(int year) {
        return taxBracketRepository.findByYearOrderByBracketStartAsc(year);
    }

    @Transactional
    public List<TaxBracket> createBrackets(TaxBracketRequest request) {
        // Delete existing brackets for the year
        List<TaxBracket> existing = taxBracketRepository.findByYearOrderByBracketStartAsc(request.getYear());
        taxBracketRepository.deleteAll(existing);

        List<TaxBracket> brackets = request.getBrackets().stream()
                .map(entry -> TaxBracket.builder()
                        .year(request.getYear())
                        .bracketStart(entry.getBracketStart())
                        .bracketEnd(entry.getBracketEnd())
                        .rate(entry.getRate())
                        .quickDeduction(entry.getQuickDeduction())
                        .build())
                .toList();

        return taxBracketRepository.saveAll(brackets);
    }

    public BigDecimal calculateTax(int year, BigDecimal monthlyTaxableIncome) {
        List<TaxBracket> brackets = taxBracketRepository.findByYearOrderByBracketStartAsc(year);

        if (brackets.isEmpty()) {
            // Fallback to 2025 brackets if no data for requested year
            brackets = taxBracketRepository.findByYearOrderByBracketStartAsc(2025);
        }

        if (monthlyTaxableIncome.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        for (int i = brackets.size() - 1; i >= 0; i--) {
            TaxBracket bracket = brackets.get(i);
            if (monthlyTaxableIncome.compareTo(bracket.getBracketStart()) > 0) {
                return monthlyTaxableIncome
                        .multiply(bracket.getRate())
                        .subtract(bracket.getQuickDeduction())
                        .setScale(0, RoundingMode.HALF_UP);
            }
        }

        return BigDecimal.ZERO;
    }
}
