package com.payroll.payroll.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TaxBracketRequest {
    private int year;
    private List<BracketEntry> brackets;

    @Data
    public static class BracketEntry {
        private BigDecimal bracketStart;
        private BigDecimal bracketEnd;
        private BigDecimal rate;
        private BigDecimal quickDeduction;
    }
}
