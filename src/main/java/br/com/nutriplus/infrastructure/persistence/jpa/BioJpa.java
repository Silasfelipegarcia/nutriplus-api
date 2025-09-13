package br.com.nutriplus.infrastructure.persistence.jpa;

import br.com.nutriplus.infrastructure.persistence.jpa.entity.BioMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BioJpa extends JpaRepository<BioMetricEntity, UUID> {
}