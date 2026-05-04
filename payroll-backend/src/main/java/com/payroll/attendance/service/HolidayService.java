package com.payroll.attendance.service;

import com.payroll.attendance.domain.Holiday;
import com.payroll.attendance.domain.HolidayType;
import com.payroll.attendance.dto.HolidayRequest;
import com.payroll.attendance.repository.HolidayRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;

    public List<Holiday> findByYear(Integer year) {
        return holidayRepository.findByYearOrderByHolidayDate(year);
    }

    public Holiday getById(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Holiday not found: " + id));
    }

    @Transactional
    public Holiday create(HolidayRequest request) {
        if (holidayRepository.existsByHolidayDate(request.getHolidayDate())) {
            throw new IllegalArgumentException("Holiday already exists on " + request.getHolidayDate());
        }
        Holiday holiday = Holiday.builder()
                .holidayDate(request.getHolidayDate())
                .name(request.getName())
                .holidayType(HolidayType.valueOf(request.getHolidayType()))
                .year(request.getYear())
                .build();
        return holidayRepository.save(holiday);
    }

    @Transactional
    public Holiday update(Long id, HolidayRequest request) {
        Holiday holiday = getById(id);
        if (!holiday.getHolidayDate().equals(request.getHolidayDate())
                && holidayRepository.existsByHolidayDate(request.getHolidayDate())) {
            throw new IllegalArgumentException("Holiday already exists on " + request.getHolidayDate());
        }
        holiday.update(request.getHolidayDate(), request.getName(),
                HolidayType.valueOf(request.getHolidayType()), request.getYear());
        return holidayRepository.save(holiday);
    }

    @Transactional
    public void delete(Long id) {
        Holiday holiday = getById(id);
        holidayRepository.delete(holiday);
    }
}
