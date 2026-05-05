package com.payroll.payroll.service;

import com.payroll.payroll.domain.InsuranceRate;
import com.payroll.payroll.dto.InsuranceRateRequest;
import com.payroll.payroll.repository.InsuranceRateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceRateService {

    private final InsuranceRateRepository insuranceRateRepository;

    public List<InsuranceRate> getAll() {
        return insuranceRateRepository.findAllByOrderByEffectiveDateDesc();
    }

    public InsuranceRate getById(Long id) {
        return insuranceRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Insurance rate not found: " + id));
    }

    public InsuranceRate getApplicableRate(LocalDate date) {
        return insuranceRateRepository
                .findTopByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(date)
                .orElseThrow(() -> new EntityNotFoundException("No applicable insurance rate found for date: " + date));
    }

    @Transactional
    public InsuranceRate create(InsuranceRateRequest request) {
        InsuranceRate rate = InsuranceRate.builder()
                .effectiveDate(request.getEffectiveDate())
                .description(request.getDescription())
                .laborRate(request.getLaborRate())
                .employmentInsuranceRate(request.getEmploymentInsuranceRate())
                .occupationalRate(request.getOccupationalRate())
                .employeeLaborShare(request.getEmployeeLaborShare())
                .employerLaborShare(request.getEmployerLaborShare())
                .healthRate(request.getHealthRate())
                .healthEmployeeShare(request.getHealthEmployeeShare())
                .healthEmployerShare(request.getHealthEmployerShare())
                .pensionRate(request.getPensionRate())
                .build();

        return insuranceRateRepository.save(rate);
    }
}
