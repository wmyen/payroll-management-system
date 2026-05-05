package com.payroll.payroll.service;

import com.payroll.department.domain.Department;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import com.payroll.payroll.domain.PayrollPeriod;
import com.payroll.payroll.domain.PayrollRecord;
import com.payroll.payroll.repository.PayrollPeriodRepository;
import com.payroll.payroll.repository.PayrollRecordRepository;
import com.payroll.shared.util.EncryptionUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final EmployeeRepository employeeRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional(readOnly = true)
    public Map<String, Object> getPayrollSummary(Long periodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Period not found: " + periodId));

        List<PayrollRecord> records = payrollRecordRepository.findByPeriodId(periodId);
        Map<Long, Employee> employees = batchLoadEmployees(records);

        Map<String, List<PayrollRecord>> byDept = records.stream()
                .collect(Collectors.groupingBy(r -> {
                    Employee emp = employees.get(r.getEmployeeId());
                    return emp != null && emp.getDepartment() != null
                            ? emp.getDepartment().getName() : "未指派部門";
                }));

        List<Map<String, Object>> deptSummaries = new ArrayList<>();
        Map<String, BigDecimal> grandTotal = initZeroMap("totalBaseSalary", "totalAllowances",
                "totalOvertimePay", "totalGrossPay", "totalDeductions", "totalNetPay", "totalEmployerCost");
        int grandEmployeeCount = 0;

        for (Map.Entry<String, List<PayrollRecord>> entry : byDept.entrySet()) {
            List<PayrollRecord> deptRecords = entry.getValue();
            Map<String, Object> dept = new LinkedHashMap<>();
            dept.put("departmentName", entry.getKey());
            dept.put("employeeCount", deptRecords.size());
            dept.put("totalBaseSalary", sum(deptRecords, PayrollRecord::getBaseSalary));
            dept.put("totalAllowances", sum(deptRecords, PayrollRecord::getTotalAllowances));
            dept.put("totalOvertimePay", sum(deptRecords, PayrollRecord::getOvertimePay));
            dept.put("totalGrossPay", sum(deptRecords, PayrollRecord::getGrossPay));
            dept.put("totalDeductions", sum(deptRecords, PayrollRecord::getTotalDeductions));
            dept.put("totalNetPay", sum(deptRecords, PayrollRecord::getNetPay));
            dept.put("totalEmployerCost", sum(deptRecords, PayrollRecord::getTotalEmployerCost));
            deptSummaries.add(dept);

            grandTotal.merge("totalBaseSalary", sum(deptRecords, PayrollRecord::getBaseSalary), BigDecimal::add);
            grandTotal.merge("totalAllowances", sum(deptRecords, PayrollRecord::getTotalAllowances), BigDecimal::add);
            grandTotal.merge("totalOvertimePay", sum(deptRecords, PayrollRecord::getOvertimePay), BigDecimal::add);
            grandTotal.merge("totalGrossPay", sum(deptRecords, PayrollRecord::getGrossPay), BigDecimal::add);
            grandTotal.merge("totalDeductions", sum(deptRecords, PayrollRecord::getTotalDeductions), BigDecimal::add);
            grandTotal.merge("totalNetPay", sum(deptRecords, PayrollRecord::getNetPay), BigDecimal::add);
            grandTotal.merge("totalEmployerCost", sum(deptRecords, PayrollRecord::getTotalEmployerCost), BigDecimal::add);
            grandEmployeeCount += deptRecords.size();
        }

        grandTotal.put("employeeCount", BigDecimal.valueOf(grandEmployeeCount));

        return Map.of(
                "period", Map.of("id", period.getId(), "year", period.getYear(), "month", period.getMonth(),
                        "startDate", period.getStartDate().toString(), "endDate", period.getEndDate().toString()),
                "departments", deptSummaries,
                "grandTotal", grandTotal
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDepartmentCost(Integer year, Integer month) {
        List<PayrollPeriod> periods;
        if (month != null) {
            periods = payrollPeriodRepository.findByYear(year).stream()
                    .filter(p -> p.getMonth() == month)
                    .collect(Collectors.toList());
        } else {
            periods = payrollPeriodRepository.findByYear(year);
        }

        List<Long> periodIds = periods.stream().map(PayrollPeriod::getId).collect(Collectors.toList());
        if (periodIds.isEmpty()) {
            return Map.of("year", year, "month", month, "departments", List.of(),
                    "companyTotal", Map.of("totalNetPay", BigDecimal.ZERO, "totalEmployerCost", BigDecimal.ZERO));
        }

        List<PayrollRecord> records = payrollRecordRepository.findByPeriodIdIn(periodIds);
        Map<Long, Employee> employees = batchLoadEmployees(records);

        Map<String, List<PayrollRecord>> byDept = records.stream()
                .collect(Collectors.groupingBy(r -> {
                    Employee emp = employees.get(r.getEmployeeId());
                    return emp != null && emp.getDepartment() != null
                            ? emp.getDepartment().getName() : "未指派部門";
                }));

        BigDecimal companyNet = BigDecimal.ZERO;
        BigDecimal companyCost = BigDecimal.ZERO;
        List<Map<String, Object>> deptList = new ArrayList<>();

        for (Map.Entry<String, List<PayrollRecord>> entry : byDept.entrySet()) {
            BigDecimal netPay = sum(entry.getValue(), PayrollRecord::getNetPay);
            BigDecimal employerCost = sum(entry.getValue(), PayrollRecord::getTotalEmployerCost);
            companyNet = companyNet.add(netPay);
            companyCost = companyCost.add(employerCost);

            Map<String, Object> dept = new LinkedHashMap<>();
            dept.put("departmentName", entry.getKey());
            dept.put("employeeCount", entry.getValue().size());
            dept.put("totalNetPay", netPay);
            dept.put("totalEmployerCost", employerCost);
            deptList.add(dept);
        }

        for (Map<String, Object> dept : deptList) {
            BigDecimal net = (BigDecimal) dept.get("totalNetPay");
            BigDecimal pct = companyNet.compareTo(BigDecimal.ZERO) > 0
                    ? net.multiply(BigDecimal.valueOf(100)).divide(companyNet, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            dept.put("percentage", pct);
        }

        return Map.of("year", year, "month", month, "departments", deptList,
                "companyTotal", Map.of("totalNetPay", companyNet, "totalEmployerCost", companyCost));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOvertimeTrend(int year) {
        List<PayrollPeriod> periods = payrollPeriodRepository.findByYear(year);
        List<Long> periodIds = periods.stream().map(PayrollPeriod::getId).collect(Collectors.toList());

        if (periodIds.isEmpty()) {
            return Map.of("year", year, "monthlyData", List.of(),
                    "yearTotal", Map.of("totalOvertimePay", BigDecimal.ZERO, "avgPerMonth", BigDecimal.ZERO));
        }

        List<PayrollRecord> records = payrollRecordRepository.findByPeriodIdIn(periodIds);
        Map<Long, Integer> periodMonthMap = periods.stream()
                .collect(Collectors.toMap(PayrollPeriod::getId, PayrollPeriod::getMonth));

        Map<Integer, List<PayrollRecord>> byMonth = records.stream()
                .collect(Collectors.groupingBy(r -> periodMonthMap.getOrDefault(r.getPeriodId(), 0)));

        List<Map<String, Object>> monthlyData = new ArrayList<>();
        BigDecimal yearTotal = BigDecimal.ZERO;

        for (int m = 1; m <= 12; m++) {
            List<PayrollRecord> monthRecords = byMonth.getOrDefault(m, List.of());
            BigDecimal overtime = sum(monthRecords, PayrollRecord::getOvertimePay);
            yearTotal = yearTotal.add(overtime);

            Map<String, Object> monthEntry = new LinkedHashMap<>();
            monthEntry.put("month", m);
            monthEntry.put("totalOvertimePay", overtime);
            monthEntry.put("employeeCount", monthRecords.size());
            monthlyData.add(monthEntry);
        }

        BigDecimal avg = periods.isEmpty() ? BigDecimal.ZERO
                : yearTotal.divide(BigDecimal.valueOf(periods.size()), 0, RoundingMode.HALF_UP);

        return Map.of("year", year, "monthlyData", monthlyData,
                "yearTotal", Map.of("totalOvertimePay", yearTotal, "avgPerMonth", avg));
    }

    @Transactional(readOnly = true)
    public String generateBankTransfer(Long periodId) {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Period not found: " + periodId));

        List<PayrollRecord> records = payrollRecordRepository.findByPeriodId(periodId);

        StringBuilder sb = new StringBuilder();
        sb.append("銀行帳號,員工姓名,轉帳金額\n");

        for (PayrollRecord r : records) {
            Employee emp = employeeRepository.findById(r.getEmployeeId()).orElse(null);
            String name = emp != null ? emp.getName() : "未知";
            String bankAccount = "";
            if (emp != null && emp.getBankAccount() != null) {
                try {
                    bankAccount = encryptionUtil.decrypt(emp.getBankAccount());
                } catch (Exception e) {
                    bankAccount = "***";
                }
            }
            sb.append(String.format("%s,%s,%s\n", bankAccount, name, r.getNetPay().toPlainString()));
        }

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public byte[] exportPayrollExcel(Long periodId) throws Exception {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Period not found: " + periodId));

        List<PayrollRecord> records = payrollRecordRepository.findByPeriodId(periodId);
        Map<Long, Employee> employees = batchLoadEmployees(records);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("薪資總表");

            CellStyle headerStyle = wb.createCellStyle();
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            String[] headers = {"員工ID", "員工姓名", "部門", "本薪", "津貼", "加班費", "其他收入",
                    "應稅合計", "勞保", "健保", "所得稅", "請假扣薪", "其他扣項", "扣項合計",
                    "實領", "雇主勞保", "雇主健保", "勞退", "雇主成本"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (PayrollRecord r : records) {
                Employee emp = employees.get(r.getEmployeeId());
                String name = emp != null ? emp.getName() : "";
                String dept = emp != null && emp.getDepartment() != null ? emp.getDepartment().getName() : "";

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getEmployeeId());
                row.createCell(1).setCellValue(name);
                row.createCell(2).setCellValue(dept);
                setBigDecimalCell(row, 3, r.getBaseSalary());
                setBigDecimalCell(row, 4, r.getTotalAllowances());
                setBigDecimalCell(row, 5, r.getOvertimePay());
                setBigDecimalCell(row, 6, r.getOtherEarnings());
                setBigDecimalCell(row, 7, r.getGrossPay());
                setBigDecimalCell(row, 8, r.getLaborInsurance());
                setBigDecimalCell(row, 9, r.getHealthInsurance());
                setBigDecimalCell(row, 10, r.getIncomeTax());
                setBigDecimalCell(row, 11, r.getLeaveDeduction());
                setBigDecimalCell(row, 12, r.getOtherDeductions());
                setBigDecimalCell(row, 13, r.getTotalDeductions());
                setBigDecimalCell(row, 14, r.getNetPay());
                setBigDecimalCell(row, 15, r.getEmployerLaborIns());
                setBigDecimalCell(row, 16, r.getEmployerHealthIns());
                setBigDecimalCell(row, 17, r.getEmployerPension());
                setBigDecimalCell(row, 18, r.getTotalEmployerCost());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private Map<Long, Employee> batchLoadEmployees(List<PayrollRecord> records) {
        List<Long> empIds = records.stream().map(PayrollRecord::getEmployeeId).distinct().collect(Collectors.toList());
        Map<Long, Employee> map = new HashMap<>();
        for (Long empId : empIds) {
            employeeRepository.findById(empId).ifPresent(emp -> {
                if (emp.getDepartment() != null) {
                    emp.getDepartment().getName();
                }
                map.put(empId, emp);
            });
        }
        return map;
    }

    private BigDecimal sum(List<PayrollRecord> records, java.util.function.Function<PayrollRecord, BigDecimal> getter) {
        return records.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> initZeroMap(String... keys) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (String key : keys) {
            map.put(key, BigDecimal.ZERO);
        }
        return map;
    }

    private void setBigDecimalCell(Row row, int col, BigDecimal value) {
        row.createCell(col).setCellValue(value.doubleValue());
    }
}
