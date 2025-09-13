package br.com.nutriplus.domain.port.out;

import br.com.nutriplus.domain.model.BioimpedanceMetric;

public interface BioRepository {
    BioimpedanceMetric save(BioimpedanceMetric metric);
}