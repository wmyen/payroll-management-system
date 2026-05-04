package com.payroll.payroll.service;

import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.payroll.domain.ItemType;
import com.payroll.payroll.domain.PayrollItem;
import com.payroll.payroll.domain.PayrollRecord;
import com.payroll.payroll.domain.PayrollRecordStatus;
import com.payroll.payroll.dto.PayrollItemRequest;
import com.payroll.payroll.dto.PayrollRecordUpdateRequest;
import com.payroll.payroll.repository.PayrollItemRepository;
import com.payroll.payroll.repository.PayrollRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollRecordService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;

    public List<PayrollRecord> getByPeriodId(Long periodId) {
        return payrollRecordRepository.findByPeriodId(periodId);
    }

    public PayrollRecord getById(Long id) {
        return payrollRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payroll record not found: " + id));
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeForRecord(Long recordId) {
        PayrollRecord record = getById(recordId);
        Employee emp = employeeRepository.findById(record.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (emp.getDepartment() != null) {
            emp.getDepartment().getName();
        }
        return emp;
    }

    @Transactional
    public PayrollRecord update(Long id, PayrollRecordUpdateRequest request) {
        PayrollRecord record = getById(id);
        if (record.getStatus() == PayrollRecordStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot update a confirmed record");
        }

        if (request.getRemark() != null) record.updateRemark(request.getRemark());

        // Recalculate net with updated custom amounts
        BigDecimal otherEarnings = request.getOtherEarnings() != null
                ? request.getOtherEarnings() : record.getOtherEarnings();
        BigDecimal otherDeductions = request.getOtherDeductions() != null
                ? request.getOtherDeductions() : record.getOtherDeductions();

        record.setCalculationResult(
                record.getBaseSalary(), record.getTotalAllowances(),
                record.getOvertimePay(), otherEarnings,
                record.getGrossPay().add(otherEarnings).subtract(record.getOtherEarnings()),
                record.getLaborInsurance(), record.getHealthInsurance(),
                record.getIncomeTax(), record.getLeaveDeduction(), otherDeductions,
                record.getLaborInsurance().add(record.getHealthInsurance())
                        .add(record.getIncomeTax()).add(record.getLeaveDeduction()).add(otherDeductions),
                record.getGrossPay().add(otherEarnings).subtract(record.getOtherEarnings())
                        .subtract(record.getLaborInsurance()).subtract(record.getHealthInsurance())
                        .subtract(record.getIncomeTax()).subtract(record.getLeaveDeduction()).subtract(otherDeductions),
                record.getEmployerLaborIns(), record.getEmployerHealthIns(),
                record.getEmployerPension(), record.getTotalEmployerCost()
        );

        return payrollRecordRepository.save(record);
    }

    // === Payroll Items ===

    public List<PayrollItem> getItems(Long recordId) {
        return payrollItemRepository.findByPayrollRecordId(recordId);
    }

    @Transactional
    public PayrollItem addItem(Long recordId, PayrollItemRequest request) {
        PayrollRecord record = getById(recordId);
        if (record.getStatus() == PayrollRecordStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot modify items of a confirmed record");
        }

        PayrollItem item = PayrollItem.builder()
                .payrollRecordId(recordId)
                .itemType(ItemType.valueOf(request.getItemType()))
                .name(request.getName())
                .amount(request.getAmount())
                .remark(request.getRemark())
                .build();

        item = payrollItemRepository.save(item);

        // Update record totals
        refreshRecordFromItems(record);
        payrollRecordRepository.save(record);

        return item;
    }

    @Transactional
    public void deleteItem(Long itemId) {
        PayrollItem item = payrollItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Payroll item not found: " + itemId));

        PayrollRecord record = getById(item.getPayrollRecordId());
        if (record.getStatus() == PayrollRecordStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot delete items from a confirmed record");
        }

        payrollItemRepository.delete(item);
        refreshRecordFromItems(record);
        payrollRecordRepository.save(record);
    }

    private void refreshRecordFromItems(PayrollRecord record) {
        List<PayrollItem> items = payrollItemRepository.findByPayrollRecordId(record.getId());

        BigDecimal otherEarnings = items.stream()
                .filter(i -> i.getItemType() == ItemType.EARNING)
                .map(PayrollItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal otherDeductions = items.stream()
                .filter(i -> i.getItemType() == ItemType.DEDUCTION)
                .map(PayrollItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossPay = record.getBaseSalary().add(record.getTotalAllowances())
                .add(record.getOvertimePay()).add(otherEarnings);
        BigDecimal totalDeductions = record.getLaborInsurance().add(record.getHealthInsurance())
                .add(record.getIncomeTax()).add(record.getLeaveDeduction()).add(otherDeductions);
        BigDecimal netPay = grossPay.subtract(totalDeductions);

        record.setCalculationResult(
                record.getBaseSalary(), record.getTotalAllowances(),
                record.getOvertimePay(), otherEarnings, grossPay,
                record.getLaborInsurance(), record.getHealthInsurance(),
                record.getIncomeTax(), record.getLeaveDeduction(),
                otherDeductions, totalDeductions, netPay,
                record.getEmployerLaborIns(), record.getEmployerHealthIns(),
                record.getEmployerPension(), record.getTotalEmployerCost()
        );
    }
}
