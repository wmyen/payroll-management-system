package com.payroll.payroll.domain;

import com.payroll.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pay_tax_bracket")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TaxBracket extends BaseEntity {

    @Column(nullable = false)
    private int year;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bracketStart;

    @Column(precision = 15, scale = 2)
    private BigDecimal bracketEnd;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal quickDeduction = BigDecimal.ZERO;
}
