package com.payroll.attendance.service;

import com.payroll.attendance.domain.AttendanceRecord;
import com.payroll.attendance.domain.AttendanceStatus;
import com.payroll.attendance.domain.HolidayType;
import com.payroll.attendance.repository.AttendanceRecordRepository;
import com.payroll.attendance.repository.HolidayRepository;
import com.payroll.employee.domain.Employee;
import com.payroll.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceRecordService {

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);
    private static final BigDecimal LUNCH_HOURS = BigDecimal.ONE;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;

    public AttendanceRecord getById(Long id) {
        return attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance record not found: " + id));
    }

    public Page<AttendanceRecord> search(Long employeeId, Long departmentId,
                                          LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return attendanceRecordRepository.search(employeeId, departmentId, startDate, endDate, pageable);
    }

    @Transactional
    public List<AttendanceRecord> importCsv(MultipartFile file) {
        List<AttendanceRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length < 4) continue;

                Long empId = Long.parseLong(cols[0].trim());
                LocalDate date = LocalDate.parse(cols[1].trim(), DATE_FMT);
                LocalTime clockIn = cols[2].isBlank() ? null : LocalTime.parse(cols[2].trim(), TIME_FMT);
                LocalTime clockOut = cols[3].isBlank() ? null : LocalTime.parse(cols[3].trim(), TIME_FMT);

                Employee employee = employeeRepository.findById(empId)
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + empId));

                AttendanceStatus status = determineStatus(date, clockIn, clockOut);
                BigDecimal workHours = calculateWorkHours(clockIn, clockOut);

                AttendanceRecord record = attendanceRecordRepository
                        .findByEmployeeIdAndRecordDate(empId, date)
                        .orElse(AttendanceRecord.builder()
                                .employee(employee)
                                .recordDate(date)
                                .build());

                record.updateClockRecord(clockIn, clockOut, workHours, status);
                records.add(attendanceRecordRepository.save(record));
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV import failed: " + e.getMessage(), e);
        }
        return records;
    }

    BigDecimal calculateWorkHours(LocalTime clockIn, LocalTime clockOut) {
        if (clockIn == null || clockOut == null) return BigDecimal.ZERO;
        long minutes = java.time.Duration.between(clockIn, clockOut).toMinutes();
        BigDecimal totalHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP);
        BigDecimal result = totalHours.subtract(LUNCH_HOURS);
        return result.compareTo(BigDecimal.ZERO) > 0 ? result : BigDecimal.ZERO;
    }

    AttendanceStatus determineStatus(LocalDate date, LocalTime clockIn, LocalTime clockOut) {
        // Check holiday
        var holidayOpt = holidayRepository.findByHolidayDate(date);
        if (holidayOpt.isPresent() && holidayOpt.get().getHolidayType() == HolidayType.HOLIDAY) {
            return AttendanceStatus.HOLIDAY;
        }

        // Check weekend (non-makeup workday)
        DayOfWeek dow = date.getDayOfWeek();
        boolean isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        if (isWeekend && (holidayOpt.isEmpty() || holidayOpt.get().getHolidayType() != HolidayType.MAKEUP_WORKDAY)) {
            return AttendanceStatus.DAY_OFF;
        }

        // No clock data = absent
        if (clockIn == null && clockOut == null) return AttendanceStatus.ABSENT;

        // Check late/early leave
        boolean late = clockIn != null && clockIn.isAfter(WORK_START);
        boolean earlyLeave = clockOut != null && clockOut.isBefore(WORK_END);
        if (late && earlyLeave) return AttendanceStatus.LATE; // prioritize late
        if (late) return AttendanceStatus.LATE;
        if (earlyLeave) return AttendanceStatus.EARLY_LEAVE;

        return AttendanceStatus.NORMAL;
    }
}
