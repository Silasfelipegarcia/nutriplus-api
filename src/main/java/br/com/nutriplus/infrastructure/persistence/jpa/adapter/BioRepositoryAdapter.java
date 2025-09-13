package br.com.nutriplus.infrastructure.persistence.jpa.adapter;

import br.com.nutriplus.domain.model.BioimpedanceMetric;
import br.com.nutriplus.domain.port.out.BioRepository;
import br.com.nutriplus.infrastructure.persistence.jpa.BioJpa;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.BioMetricEntity;
import org.springframework.stereotype.Component;

@Component
public class BioRepositoryAdapter implements BioRepository {
    private final BioJpa bioJpa;

    public BioRepositoryAdapter(BioJpa bioJpa) {
        this.bioJpa = bioJpa;
    }

    @Override
    public BioimpedanceMetric save(BioimpedanceMetric metric) {
        BioMetricEntity entity = new BioMetricEntity();
        entity.setId(metric.getId());
        entity.setUserId(metric.getUserId());
        entity.setReportDate(metric.getReportDate());
        entity.setWeightKg(metric.getWeightKg());
        entity.setBodyFatPercent(metric.getBodyFatPercent());
        entity.setSkeletalMuscleMassKg(metric.getSkeletalMuscleMassKg());
        entity.setPhaseAngleDeg(metric.getPhaseAngleDeg());
        entity.setNotes(metric.getNotes());
        bioJpa.save(entity);
        return metric;
    }
}