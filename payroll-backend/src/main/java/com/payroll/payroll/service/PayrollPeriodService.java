package com.payroll.payroll.service;

import com.payroll.payroll.domain.PayrollPeriod;
import com.payroll.payroll.domain.PayrollPeriodStatus;
import com.payroll.payroll.domain.PayrollRecord;
import com.payroll.payroll.dto.PayrollPeriodRequest;
import com.payroll.payroll.repository.PayrollPeriodRepository;
import com.payroll.payroll.repository.PayrollRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollPeriodService {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final PayrollCalculationService calculationService;

    public List<PayrollPeriod> getAll() {
        return payrollPeriodRepository.findAllByOrderByYearDescMonthDesc();
    }

    public PayrollPeriod getById(Long id) {
        return payrollPeriodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payroll period not found: " + id));
    }

    @Transactional
    public PayrollPeriod create(PayrollPeriodRequest request) {
        payrollPeriodRepository.findByYearAndMonth(request.getYear(), request.getMonth())
                .ifPresent(p -> { throw new IllegalStateException(
                        "Period already exists for " + request.getYear() + "/" + request.getMonth()); });

        PayrollPeriod period = PayrollPeriod.builder()
                .year(request.getYear())
                .month(request.getMonth())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .payDate(request.getPayDate())
                .build();

        return payrollPeriodRepository.save(period);
    }

    @Transactional
    public PayrollPeriod update(Long id, PayrollPeriodRequest request) {
        PayrollPeriod period = getById(id);
        if (period.getStatus() == PayrollPeriodStatus.LOCKED) {
            throw new IllegalStateException("Cannot update a locked period");
        }
        period.update(request.getStartDate(), request.getEndDate(), request.getPayDate());
        return payrollPeriodRepository.save(period);
    }

    @Transactional
    public List<PayrollRecord> calculate(Long id) {
        PayrollPeriod period = getById(id);
        period.markProcessing();
        payrollPeriodRepository.save(period);

        List<PayrollRecord> records = calculationService.calculateForPeriod(
                id, period.getYear(), period.getMonth(),
                period.getStartDate(), period.getEndDate());

        period.confirm();
        payrollPeriodRepository.save(period);

        return records;
    }

    @Transactional
    public void confirm(Long id) {
        PayrollPeriod period = getById(id);
        if (period.getStatus() != PayrollPeriodStatus.CONFIRMED) {
            throw new IllegalStateException("Period must be in CONFIRMED state to lock");
        }
        period.lock();
        payrollPeriodRepository.save(period);

        List<PayrollRecord> records = payrollRecordRepository.findByPeriodId(id);
        records.forEach(PayrollRecord::confirm);
        payrollRecordRepository.saveAll(records);
    }
}
