package br.com.nutriplus.domain.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum SportType {
    RUNNING("Corrida", 9.8),
    WALKING("Caminhada", 3.5),
    CYCLING("Ciclismo", 7.5),
    SWIMMING("Natação", 8.0),
    WEIGHT_TRAINING("Musculação", 6.0),
    CROSSFIT("CrossFit", 8.5),
    FOOTBALL("Futebol", 7.0),
    YOGA("Yoga", 3.0),
    PILATES("Pilates", 3.5),
    MARTIAL_ARTS("Artes marciais", 10.0),
    DANCE("Dança", 5.0),
    HIIT("HIIT", 9.0),
    TENNIS("Tênis", 7.3),
    VOLLEYBALL("Vôlei", 6.0),
    BASKETBALL("Basquete", 6.5),
    FUNCTIONAL("Treino funcional", 6.5),
    SPINNING("Spinning", 8.0),
    HIKING("Trilha", 6.0),
    STRETCHING("Alongamento", 2.5),
    BOXING("Boxe", 9.0),
    BEACH_TENNIS("Beach tennis", 6.5),
    ROWING("Remo", 7.0),
    OTHER("Outro", 5.5);

    private final String labelPt;
    private final double met;

    SportType(String labelPt, double met) {
        this.labelPt = labelPt;
        this.met = met;
    }

    public String labelPt() {
        return labelPt;
    }

    public double met() {
        return met;
    }

    public BigDecimal caloriesPerSession(BigDecimal weightKg, int minutes) {
        if (weightKg == null || minutes <= 0) {
            return BigDecimal.ZERO;
        }
        double hours = minutes / 60.0;
        double kcal = met * weightKg.doubleValue() * hours;
        return BigDecimal.valueOf(kcal).setScale(0, RoundingMode.HALF_UP);
    }
}
