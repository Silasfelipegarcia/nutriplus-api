package br.com.nutriplus.domain.port.in;

import java.time.LocalDate;
import java.util.UUID;

public interface ManualBioUseCase {
    void insert(UUID userId, LocalDate reportDate, Double weightKg, Double bodyFatPercent, Double skeletalMuscleMassKg, Double phaseAngleDeg, String notes);
}