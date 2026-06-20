package br.com.nutriplus.domain.enums;

public enum EvolutionMetricStatus {
    EXCELLENT,
    GOOD,
    OK,
    BELOW;

    public String labelPt() {
        return switch (this) {
            case EXCELLENT -> "Ótimo";
            case GOOD -> "Acima";
            case OK -> "Ok";
            case BELOW -> "Abaixo";
        };
    }
}
