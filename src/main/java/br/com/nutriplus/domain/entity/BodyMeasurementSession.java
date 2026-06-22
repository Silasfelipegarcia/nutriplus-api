package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "body_measurement_sessions")
public class BodyMeasurementSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_nutritionist_id")
    private Nutritionist recordedByNutritionist;

    @Column(name = "measured_on", nullable = false)
    private LocalDate measuredOn;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "body_fat_percent", precision = 5, scale = 2)
    private BigDecimal bodyFatPercent;

    @Column(name = "muscle_mass_kg", precision = 5, scale = 2)
    private BigDecimal muscleMassKg;

    @Column(name = "waist_cm", precision = 5, scale = 2)
    private BigDecimal waistCm;

    @Column(name = "hip_cm", precision = 5, scale = 2)
    private BigDecimal hipCm;

    @Column(name = "chest_cm", precision = 5, scale = 2)
    private BigDecimal chestCm;

    @Column(name = "neck_cm", precision = 5, scale = 2)
    private BigDecimal neckCm;

    @Column(name = "arm_right_cm", precision = 5, scale = 2)
    private BigDecimal armRightCm;

    @Column(name = "arm_left_cm", precision = 5, scale = 2)
    private BigDecimal armLeftCm;

    @Column(name = "thigh_right_cm", precision = 5, scale = 2)
    private BigDecimal thighRightCm;

    @Column(name = "thigh_left_cm", precision = 5, scale = 2)
    private BigDecimal thighLeftCm;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected BodyMeasurementSession() {
    }

    public BodyMeasurementSession(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Nutritionist getRecordedByNutritionist() {
        return recordedByNutritionist;
    }

    public void setRecordedByNutritionist(Nutritionist recordedByNutritionist) {
        this.recordedByNutritionist = recordedByNutritionist;
    }

    public LocalDate getMeasuredOn() {
        return measuredOn;
    }

    public void setMeasuredOn(LocalDate measuredOn) {
        this.measuredOn = measuredOn;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getBodyFatPercent() {
        return bodyFatPercent;
    }

    public void setBodyFatPercent(BigDecimal bodyFatPercent) {
        this.bodyFatPercent = bodyFatPercent;
    }

    public BigDecimal getMuscleMassKg() {
        return muscleMassKg;
    }

    public void setMuscleMassKg(BigDecimal muscleMassKg) {
        this.muscleMassKg = muscleMassKg;
    }

    public BigDecimal getWaistCm() {
        return waistCm;
    }

    public void setWaistCm(BigDecimal waistCm) {
        this.waistCm = waistCm;
    }

    public BigDecimal getHipCm() {
        return hipCm;
    }

    public void setHipCm(BigDecimal hipCm) {
        this.hipCm = hipCm;
    }

    public BigDecimal getChestCm() {
        return chestCm;
    }

    public void setChestCm(BigDecimal chestCm) {
        this.chestCm = chestCm;
    }

    public BigDecimal getNeckCm() {
        return neckCm;
    }

    public void setNeckCm(BigDecimal neckCm) {
        this.neckCm = neckCm;
    }

    public BigDecimal getArmRightCm() {
        return armRightCm;
    }

    public void setArmRightCm(BigDecimal armRightCm) {
        this.armRightCm = armRightCm;
    }

    public BigDecimal getArmLeftCm() {
        return armLeftCm;
    }

    public void setArmLeftCm(BigDecimal armLeftCm) {
        this.armLeftCm = armLeftCm;
    }

    public BigDecimal getThighRightCm() {
        return thighRightCm;
    }

    public void setThighRightCm(BigDecimal thighRightCm) {
        this.thighRightCm = thighRightCm;
    }

    public BigDecimal getThighLeftCm() {
        return thighLeftCm;
    }

    public void setThighLeftCm(BigDecimal thighLeftCm) {
        this.thighLeftCm = thighLeftCm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
