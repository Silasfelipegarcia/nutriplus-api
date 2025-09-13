package br.com.nutriplus.application.service;

import br.com.nutriplus.domain.model.BioimpedanceMetric;
import br.com.nutriplus.domain.port.in.ManualBioUseCase;
import br.com.nutriplus.domain.port.out.BioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class BioService implements ManualBioUseCase {

    private final BioRepository bioRepository;

    public BioService(BioRepository bioRepository) {
        this.bioRepository = bioRepository;
    }

    @Override
    public void insert(UUID userId, LocalDate reportDate, Double weightKg, Double bodyFatPercent, Double skeletalMuscleMassKg, Double phaseAngleDeg, String notes) {
        BioimpedanceMetric metric = new BioimpedanceMetric(UUID.randomUUID(), userId, reportDate, weightKg, bodyFatPercent, skeletalMuscleMassKg, phaseAngleDeg, notes);
        bioRepository.save(metric);
    }

}