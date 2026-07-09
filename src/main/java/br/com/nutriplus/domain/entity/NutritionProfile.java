package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "nutrition_profiles")
public class NutritionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(length = 120)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "chewing_difficulty", nullable = false)
    private ChewingDifficulty chewingDifficulty = ChewingDifficulty.NONE;

    @Column(name = "senior_weight_loss_ack", nullable = false)
    private boolean seniorWeightLossAck = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Column(name = "height_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "current_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal currentWeightKg;

    @Column(name = "target_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    @Column(name = "goal_target_weeks")
    private Integer goalTargetWeeks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_preference", nullable = false)
    private DietaryPreference dietaryPreference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Restriction restriction;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_persona", nullable = false)
    private AgentPersona agentPersona = AgentPersona.LUNA;

    @Column(name = "food_likes", columnDefinition = "TEXT")
    private String foodLikes;

    @Column(name = "food_dislikes", columnDefinition = "TEXT")
    private String foodDislikes;

    @Column(name = "meal_notes", columnDefinition = "TEXT")
    private String mealNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_budget_level", nullable = false)
    private FoodBudgetLevel foodBudgetLevel = FoodBudgetLevel.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false)
    private CalculationMethod calculationMethod = CalculationMethod.ESTIMATE;

    @Column(name = "body_fat_percent", precision = 5, scale = 2)
    private BigDecimal bodyFatPercent;

    @Column(name = "manual_bmr_kcal", precision = 8, scale = 2)
    private BigDecimal manualBmrKcal;

    @Column(name = "lean_mass_kg", precision = 5, scale = 2)
    private BigDecimal leanMassKg;

    @Column(name = "muscle_mass_kg", precision = 5, scale = 2)
    private BigDecimal muscleMassKg;

    @Column(name = "progress_review_interval_days", nullable = false)
    private Integer progressReviewIntervalDays = 15;

    @Column(name = "athlete_mode_enabled", nullable = false)
    private boolean athleteModeEnabled = false;

    @Column(name = "one_time_correction_used_at")
    private LocalDateTime oneTimeCorrectionUsedAt;

    @Column(name = "last_athlete_regen_at")
    private LocalDateTime lastAthleteRegenAt;

    @Column(name = "athlete_regen_eligible", nullable = false)
    private boolean athleteRegenEligible = false;

    @Column(name = "plan_regen_locked_until")
    private LocalDate planRegenLockedUntil;

    @Column(name = "training_daily_extra_kcal", precision = 8, scale = 2)
    private BigDecimal trainingDailyExtraKcal;

    @Column(name = "bmr_kcal", precision = 8, scale = 2)
    private BigDecimal bmrKcal;

    @Column(name = "tdee_kcal", precision = 8, scale = 2)
    private BigDecimal tdeeKcal;

    @Column(name = "target_calories", precision = 8, scale = 2)
    private BigDecimal targetCalories;

    @Column(name = "target_protein_g", precision = 8, scale = 2)
    private BigDecimal targetProteinG;

    @Column(name = "target_carbs_g", precision = 8, scale = 2)
    private BigDecimal targetCarbsG;

    @Column(name = "target_fat_g", precision = 8, scale = 2)
    private BigDecimal targetFatG;

    @Column(name = "pace_warning", length = 500)
    private String paceWarning;

    @Column(name = "estimated_weekly_rate_kg", precision = 4, scale = 2)
    private BigDecimal estimatedWeeklyRateKg;

    @Column(name = "daily_water_target_ml")
    private Integer dailyWaterTargetMl;

    @Column(name = "wake_time")
    private LocalTime wakeTime;

    @Column(name = "sleep_time")
    private LocalTime sleepTime;

    @Column(name = "primary_training_time")
    private LocalTime primaryTrainingTime;

    @Column(name = "athlete_hunger_json", length = 400)
    private String athleteHungerJson;

    @Column(name = "health_conditions", columnDefinition = "TEXT")
    private String healthConditions;

    @Column(columnDefinition = "TEXT")
    private String medications;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "health_notes", columnDefinition = "TEXT")
    private String healthNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "pregnancy_status", nullable = false)
    private PregnancyStatus pregnancyStatus = PregnancyStatus.NONE;

    @Column(name = "eating_disorder_risk", nullable = false)
    private boolean eatingDisorderRisk = false;

    @Column(name = "severe_renal_restriction", nullable = false)
    private boolean severeRenalRestriction = false;

    @Column(name = "ai_plan_eligible", nullable = false)
    private boolean aiPlanEligible = true;

    @Column(name = "ai_plan_ineligible_reason", length = 30)
    private String aiPlanIneligibleReason;

    @Column(name = "health_eligibility_ack_at")
    private LocalDateTime healthEligibilityAckAt;

    @Column(name = "health_eligibility_version", length = 20)
    private String healthEligibilityVersion;

    @Column(name = "eats_breakfast", nullable = false)
    private boolean eatsBreakfast = true;

    @Column(name = "eats_lunch", nullable = false)
    private boolean eatsLunch = true;

    @Column(name = "eats_afternoon_snack", nullable = false)
    private boolean eatsAfternoonSnack = false;

    @Column(name = "eats_dinner", nullable = false)
    private boolean eatsDinner = true;

    @Column(name = "open_to_routine_adjustment", nullable = false)
    private boolean openToRoutineAdjustment = false;

    @Column(name = "free_extras_json", columnDefinition = "json")
    private String freeExtrasJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "hunger_pattern", nullable = false, length = 20)
    private HungerPattern hungerPattern = HungerPattern.BALANCED;

    @Enumerated(EnumType.STRING)
    @Column(name = "nutrition_mode", nullable = false, length = 30)
    private NutritionMode nutritionMode = NutritionMode.STANDARD;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NutritionProfile() {
    }

    private NutritionProfile(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.age = builder.age;
        this.birthDate = builder.birthDate;
        this.stateCode = builder.stateCode;
        this.city = builder.city;
        this.chewingDifficulty = builder.chewingDifficulty != null ? builder.chewingDifficulty : ChewingDifficulty.NONE;
        this.seniorWeightLossAck = builder.seniorWeightLossAck;
        this.sex = builder.sex;
        this.heightCm = builder.heightCm;
        this.currentWeightKg = builder.currentWeightKg;
        this.targetWeightKg = builder.targetWeightKg;
        this.goalTargetWeeks = builder.goalTargetWeeks;
        this.goal = builder.goal;
        this.activityLevel = builder.activityLevel;
        this.dietaryPreference = builder.dietaryPreference;
        this.restriction = builder.restriction;
        this.agentPersona = builder.agentPersona != null ? builder.agentPersona : AgentPersona.LUNA;
        this.foodLikes = builder.foodLikes;
        this.foodDislikes = builder.foodDislikes;
        this.mealNotes = builder.mealNotes;
        this.foodBudgetLevel = builder.foodBudgetLevel != null ? builder.foodBudgetLevel : FoodBudgetLevel.MODERATE;
        this.calculationMethod = builder.calculationMethod != null ? builder.calculationMethod : CalculationMethod.ESTIMATE;
        this.bodyFatPercent = builder.bodyFatPercent;
        this.manualBmrKcal = builder.manualBmrKcal;
        this.leanMassKg = builder.leanMassKg;
        this.muscleMassKg = builder.muscleMassKg;
        this.progressReviewIntervalDays = builder.progressReviewIntervalDays != null
                ? builder.progressReviewIntervalDays : 15;
        this.athleteModeEnabled = builder.athleteModeEnabled;
        this.oneTimeCorrectionUsedAt = builder.oneTimeCorrectionUsedAt;
        this.lastAthleteRegenAt = builder.lastAthleteRegenAt;
        this.athleteRegenEligible = builder.athleteRegenEligible;
        this.planRegenLockedUntil = builder.planRegenLockedUntil;
        this.trainingDailyExtraKcal = builder.trainingDailyExtraKcal;
        this.bmrKcal = builder.bmrKcal;
        this.tdeeKcal = builder.tdeeKcal;
        this.targetCalories = builder.targetCalories;
        this.targetProteinG = builder.targetProteinG;
        this.targetCarbsG = builder.targetCarbsG;
        this.targetFatG = builder.targetFatG;
        this.paceWarning = builder.paceWarning;
        this.estimatedWeeklyRateKg = builder.estimatedWeeklyRateKg;
        this.dailyWaterTargetMl = builder.dailyWaterTargetMl;
        this.wakeTime = builder.wakeTime;
        this.sleepTime = builder.sleepTime;
        this.primaryTrainingTime = builder.primaryTrainingTime;
        this.athleteHungerJson = builder.athleteHungerJson;
        this.healthConditions = builder.healthConditions;
        this.medications = builder.medications;
        this.allergies = builder.allergies;
        this.healthNotes = builder.healthNotes;
        this.pregnancyStatus = builder.pregnancyStatus != null ? builder.pregnancyStatus : PregnancyStatus.NONE;
        this.eatingDisorderRisk = builder.eatingDisorderRisk;
        this.severeRenalRestriction = builder.severeRenalRestriction;
        this.aiPlanEligible = builder.aiPlanEligible;
        this.aiPlanIneligibleReason = builder.aiPlanIneligibleReason;
        this.healthEligibilityAckAt = builder.healthEligibilityAckAt;
        this.healthEligibilityVersion = builder.healthEligibilityVersion;
        this.eatsBreakfast = builder.eatsBreakfast;
        this.eatsLunch = builder.eatsLunch;
        this.eatsAfternoonSnack = builder.eatsAfternoonSnack;
        this.eatsDinner = builder.eatsDinner;
        this.openToRoutineAdjustment = builder.openToRoutineAdjustment;
        this.freeExtrasJson = builder.freeExtrasJson;
        this.hungerPattern = builder.hungerPattern != null ? builder.hungerPattern : HungerPattern.BALANCED;
        this.nutritionMode = builder.nutritionMode != null ? builder.nutritionMode : NutritionMode.STANDARD;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public ChewingDifficulty getChewingDifficulty() {
        return chewingDifficulty;
    }

    public void setChewingDifficulty(ChewingDifficulty chewingDifficulty) {
        this.chewingDifficulty = chewingDifficulty != null ? chewingDifficulty : ChewingDifficulty.NONE;
    }

    public boolean isSeniorWeightLossAck() {
        return seniorWeightLossAck;
    }

    public void setSeniorWeightLossAck(boolean seniorWeightLossAck) {
        this.seniorWeightLossAck = seniorWeightLossAck;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(BigDecimal currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public BigDecimal getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(BigDecimal targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public Integer getGoalTargetWeeks() {
        return goalTargetWeeks;
    }

    public void setGoalTargetWeeks(Integer goalTargetWeeks) {
        this.goalTargetWeeks = goalTargetWeeks;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public DietaryPreference getDietaryPreference() {
        return dietaryPreference;
    }

    public void setDietaryPreference(DietaryPreference dietaryPreference) {
        this.dietaryPreference = dietaryPreference;
    }

    public Restriction getRestriction() {
        return restriction;
    }

    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
    }

    public AgentPersona getAgentPersona() {
        return agentPersona;
    }

    public void setAgentPersona(AgentPersona agentPersona) {
        this.agentPersona = agentPersona;
    }

    public String getFoodLikes() {
        return foodLikes;
    }

    public void setFoodLikes(String foodLikes) {
        this.foodLikes = foodLikes;
    }

    public String getFoodDislikes() {
        return foodDislikes;
    }

    public void setFoodDislikes(String foodDislikes) {
        this.foodDislikes = foodDislikes;
    }

    public String getMealNotes() {
        return mealNotes;
    }

    public void setMealNotes(String mealNotes) {
        this.mealNotes = mealNotes;
    }

    public FoodBudgetLevel getFoodBudgetLevel() {
        return foodBudgetLevel;
    }

    public void setFoodBudgetLevel(FoodBudgetLevel foodBudgetLevel) {
        this.foodBudgetLevel = foodBudgetLevel != null ? foodBudgetLevel : FoodBudgetLevel.MODERATE;
    }

    public CalculationMethod getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(CalculationMethod calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public BigDecimal getBodyFatPercent() {
        return bodyFatPercent;
    }

    public void setBodyFatPercent(BigDecimal bodyFatPercent) {
        this.bodyFatPercent = bodyFatPercent;
    }

    public BigDecimal getManualBmrKcal() {
        return manualBmrKcal;
    }

    public void setManualBmrKcal(BigDecimal manualBmrKcal) {
        this.manualBmrKcal = manualBmrKcal;
    }

    public BigDecimal getLeanMassKg() {
        return leanMassKg;
    }

    public void setLeanMassKg(BigDecimal leanMassKg) {
        this.leanMassKg = leanMassKg;
    }

    public BigDecimal getMuscleMassKg() {
        return muscleMassKg;
    }

    public void setMuscleMassKg(BigDecimal muscleMassKg) {
        this.muscleMassKg = muscleMassKg;
    }

    public Integer getProgressReviewIntervalDays() {
        return progressReviewIntervalDays != null ? progressReviewIntervalDays : 15;
    }

    public void setProgressReviewIntervalDays(Integer progressReviewIntervalDays) {
        this.progressReviewIntervalDays = progressReviewIntervalDays;
    }

    public boolean isAthleteModeEnabled() {
        return athleteModeEnabled;
    }

    public void setAthleteModeEnabled(boolean athleteModeEnabled) {
        this.athleteModeEnabled = athleteModeEnabled;
    }

    public LocalDateTime getOneTimeCorrectionUsedAt() {
        return oneTimeCorrectionUsedAt;
    }

    public void setOneTimeCorrectionUsedAt(LocalDateTime oneTimeCorrectionUsedAt) {
        this.oneTimeCorrectionUsedAt = oneTimeCorrectionUsedAt;
    }

    public LocalDateTime getLastAthleteRegenAt() {
        return lastAthleteRegenAt;
    }

    public void setLastAthleteRegenAt(LocalDateTime lastAthleteRegenAt) {
        this.lastAthleteRegenAt = lastAthleteRegenAt;
    }

    public boolean isAthleteRegenEligible() {
        return athleteRegenEligible;
    }

    public void setAthleteRegenEligible(boolean athleteRegenEligible) {
        this.athleteRegenEligible = athleteRegenEligible;
    }

    public LocalDate getPlanRegenLockedUntil() {
        return planRegenLockedUntil;
    }

    public void setPlanRegenLockedUntil(LocalDate planRegenLockedUntil) {
        this.planRegenLockedUntil = planRegenLockedUntil;
    }

    public BigDecimal getTrainingDailyExtraKcal() {
        return trainingDailyExtraKcal;
    }

    public void setTrainingDailyExtraKcal(BigDecimal trainingDailyExtraKcal) {
        this.trainingDailyExtraKcal = trainingDailyExtraKcal;
    }

    public BigDecimal getBmrKcal() {
        return bmrKcal;
    }

    public void setBmrKcal(BigDecimal bmrKcal) {
        this.bmrKcal = bmrKcal;
    }

    public BigDecimal getTdeeKcal() {
        return tdeeKcal;
    }

    public void setTdeeKcal(BigDecimal tdeeKcal) {
        this.tdeeKcal = tdeeKcal;
    }

    public BigDecimal getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(BigDecimal targetCalories) {
        this.targetCalories = targetCalories;
    }

    public BigDecimal getTargetProteinG() {
        return targetProteinG;
    }

    public void setTargetProteinG(BigDecimal targetProteinG) {
        this.targetProteinG = targetProteinG;
    }

    public BigDecimal getTargetCarbsG() {
        return targetCarbsG;
    }

    public void setTargetCarbsG(BigDecimal targetCarbsG) {
        this.targetCarbsG = targetCarbsG;
    }

    public BigDecimal getTargetFatG() {
        return targetFatG;
    }

    public void setTargetFatG(BigDecimal targetFatG) {
        this.targetFatG = targetFatG;
    }

    public String getPaceWarning() {
        return paceWarning;
    }

    public void setPaceWarning(String paceWarning) {
        this.paceWarning = paceWarning;
    }

    public BigDecimal getEstimatedWeeklyRateKg() {
        return estimatedWeeklyRateKg;
    }

    public void setEstimatedWeeklyRateKg(BigDecimal estimatedWeeklyRateKg) {
        this.estimatedWeeklyRateKg = estimatedWeeklyRateKg;
    }

    public Integer getDailyWaterTargetMl() {
        return dailyWaterTargetMl;
    }

    public void setDailyWaterTargetMl(Integer dailyWaterTargetMl) {
        this.dailyWaterTargetMl = dailyWaterTargetMl;
    }

    public LocalTime getWakeTime() {
        return wakeTime;
    }

    public void setWakeTime(LocalTime wakeTime) {
        this.wakeTime = wakeTime;
    }

    public LocalTime getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(LocalTime sleepTime) {
        this.sleepTime = sleepTime;
    }

    public LocalTime getPrimaryTrainingTime() {
        return primaryTrainingTime;
    }

    public void setPrimaryTrainingTime(LocalTime primaryTrainingTime) {
        this.primaryTrainingTime = primaryTrainingTime;
    }

    public String getAthleteHungerJson() {
        return athleteHungerJson;
    }

    public void setAthleteHungerJson(String athleteHungerJson) {
        this.athleteHungerJson = athleteHungerJson;
    }

    public String getHealthConditions() {
        return healthConditions;
    }

    public void setHealthConditions(String healthConditions) {
        this.healthConditions = healthConditions;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getHealthNotes() {
        return healthNotes;
    }

    public void setHealthNotes(String healthNotes) {
        this.healthNotes = healthNotes;
    }

    public PregnancyStatus getPregnancyStatus() {
        return pregnancyStatus;
    }

    public void setPregnancyStatus(PregnancyStatus pregnancyStatus) {
        this.pregnancyStatus = pregnancyStatus;
    }

    public boolean isEatingDisorderRisk() {
        return eatingDisorderRisk;
    }

    public void setEatingDisorderRisk(boolean eatingDisorderRisk) {
        this.eatingDisorderRisk = eatingDisorderRisk;
    }

    public boolean isSevereRenalRestriction() {
        return severeRenalRestriction;
    }

    public void setSevereRenalRestriction(boolean severeRenalRestriction) {
        this.severeRenalRestriction = severeRenalRestriction;
    }

    public boolean isAiPlanEligible() {
        return aiPlanEligible;
    }

    public void setAiPlanEligible(boolean aiPlanEligible) {
        this.aiPlanEligible = aiPlanEligible;
    }

    public String getAiPlanIneligibleReason() {
        return aiPlanIneligibleReason;
    }

    public void setAiPlanIneligibleReason(String aiPlanIneligibleReason) {
        this.aiPlanIneligibleReason = aiPlanIneligibleReason;
    }

    public LocalDateTime getHealthEligibilityAckAt() {
        return healthEligibilityAckAt;
    }

    public void setHealthEligibilityAckAt(LocalDateTime healthEligibilityAckAt) {
        this.healthEligibilityAckAt = healthEligibilityAckAt;
    }

    public String getHealthEligibilityVersion() {
        return healthEligibilityVersion;
    }

    public void setHealthEligibilityVersion(String healthEligibilityVersion) {
        this.healthEligibilityVersion = healthEligibilityVersion;
    }

    public boolean isEatsBreakfast() {
        return eatsBreakfast;
    }

    public void setEatsBreakfast(boolean eatsBreakfast) {
        this.eatsBreakfast = eatsBreakfast;
    }

    public boolean isEatsLunch() {
        return eatsLunch;
    }

    public void setEatsLunch(boolean eatsLunch) {
        this.eatsLunch = eatsLunch;
    }

    public boolean isEatsAfternoonSnack() {
        return eatsAfternoonSnack;
    }

    public void setEatsAfternoonSnack(boolean eatsAfternoonSnack) {
        this.eatsAfternoonSnack = eatsAfternoonSnack;
    }

    public boolean isEatsDinner() {
        return eatsDinner;
    }

    public void setEatsDinner(boolean eatsDinner) {
        this.eatsDinner = eatsDinner;
    }

    public boolean isOpenToRoutineAdjustment() {
        return openToRoutineAdjustment;
    }

    public void setOpenToRoutineAdjustment(boolean openToRoutineAdjustment) {
        this.openToRoutineAdjustment = openToRoutineAdjustment;
    }

    public String getFreeExtrasJson() {
        return freeExtrasJson;
    }

    public void setFreeExtrasJson(String freeExtrasJson) {
        this.freeExtrasJson = freeExtrasJson;
    }

    public HungerPattern getHungerPattern() {
        return hungerPattern;
    }

    public void setHungerPattern(HungerPattern hungerPattern) {
        this.hungerPattern = hungerPattern != null ? hungerPattern : HungerPattern.BALANCED;
    }

    public NutritionMode getNutritionMode() {
        return nutritionMode;
    }

    public void setNutritionMode(NutritionMode nutritionMode) {
        this.nutritionMode = nutritionMode != null ? nutritionMode : NutritionMode.STANDARD;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private User user;
        private Integer age;
        private LocalDate birthDate;
        private String stateCode;
        private String city;
        private ChewingDifficulty chewingDifficulty = ChewingDifficulty.NONE;
        private boolean seniorWeightLossAck;
        private Sex sex;
        private BigDecimal heightCm;
        private BigDecimal currentWeightKg;
        private BigDecimal targetWeightKg;
        private Integer goalTargetWeeks;
        private Goal goal;
        private ActivityLevel activityLevel;
        private DietaryPreference dietaryPreference;
        private Restriction restriction;
        private AgentPersona agentPersona;
        private String foodLikes;
        private String foodDislikes;
        private String mealNotes;
        private FoodBudgetLevel foodBudgetLevel = FoodBudgetLevel.MODERATE;
        private CalculationMethod calculationMethod;
        private BigDecimal bodyFatPercent;
        private BigDecimal manualBmrKcal;
        private BigDecimal leanMassKg;
        private BigDecimal muscleMassKg;
        private Integer progressReviewIntervalDays;
        private boolean athleteModeEnabled;
        private LocalDateTime oneTimeCorrectionUsedAt;
        private LocalDateTime lastAthleteRegenAt;
        private boolean athleteRegenEligible;
        private LocalDate planRegenLockedUntil;
        private BigDecimal trainingDailyExtraKcal;
        private BigDecimal bmrKcal;
        private BigDecimal tdeeKcal;
        private BigDecimal targetCalories;
        private BigDecimal targetProteinG;
        private BigDecimal targetCarbsG;
        private BigDecimal targetFatG;
        private String paceWarning;
        private BigDecimal estimatedWeeklyRateKg;
        private Integer dailyWaterTargetMl;
        private LocalTime wakeTime;
        private LocalTime sleepTime;
        private LocalTime primaryTrainingTime;
        private String athleteHungerJson;
        private String healthConditions;
        private String medications;
        private String allergies;
        private String healthNotes;
        private PregnancyStatus pregnancyStatus = PregnancyStatus.NONE;
        private boolean eatingDisorderRisk;
        private boolean severeRenalRestriction;
        private boolean aiPlanEligible = true;
        private String aiPlanIneligibleReason;
        private LocalDateTime healthEligibilityAckAt;
        private String healthEligibilityVersion;
        private boolean eatsBreakfast = true;
        private boolean eatsLunch = true;
        private boolean eatsAfternoonSnack = false;
        private boolean eatsDinner = true;
        private boolean openToRoutineAdjustment = false;
        private String freeExtrasJson;
        private HungerPattern hungerPattern = HungerPattern.BALANCED;
        private NutritionMode nutritionMode = NutritionMode.STANDARD;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        public Builder birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder stateCode(String stateCode) {
            this.stateCode = stateCode;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder chewingDifficulty(ChewingDifficulty chewingDifficulty) {
            this.chewingDifficulty = chewingDifficulty;
            return this;
        }

        public Builder seniorWeightLossAck(boolean seniorWeightLossAck) {
            this.seniorWeightLossAck = seniorWeightLossAck;
            return this;
        }

        public Builder sex(Sex sex) {
            this.sex = sex;
            return this;
        }

        public Builder heightCm(BigDecimal heightCm) {
            this.heightCm = heightCm;
            return this;
        }

        public Builder currentWeightKg(BigDecimal currentWeightKg) {
            this.currentWeightKg = currentWeightKg;
            return this;
        }

        public Builder targetWeightKg(BigDecimal targetWeightKg) {
            this.targetWeightKg = targetWeightKg;
            return this;
        }

        public Builder goalTargetWeeks(Integer goalTargetWeeks) {
            this.goalTargetWeeks = goalTargetWeeks;
            return this;
        }

        public Builder goal(Goal goal) {
            this.goal = goal;
            return this;
        }

        public Builder activityLevel(ActivityLevel activityLevel) {
            this.activityLevel = activityLevel;
            return this;
        }

        public Builder dietaryPreference(DietaryPreference dietaryPreference) {
            this.dietaryPreference = dietaryPreference;
            return this;
        }

        public Builder restriction(Restriction restriction) {
            this.restriction = restriction;
            return this;
        }

        public Builder agentPersona(AgentPersona agentPersona) {
            this.agentPersona = agentPersona;
            return this;
        }

        public Builder foodLikes(String foodLikes) {
            this.foodLikes = foodLikes;
            return this;
        }

        public Builder foodDislikes(String foodDislikes) {
            this.foodDislikes = foodDislikes;
            return this;
        }

        public Builder mealNotes(String mealNotes) {
            this.mealNotes = mealNotes;
            return this;
        }

        public Builder foodBudgetLevel(FoodBudgetLevel foodBudgetLevel) {
            this.foodBudgetLevel = foodBudgetLevel;
            return this;
        }

        public Builder calculationMethod(CalculationMethod calculationMethod) {
            this.calculationMethod = calculationMethod;
            return this;
        }

        public Builder bodyFatPercent(BigDecimal bodyFatPercent) {
            this.bodyFatPercent = bodyFatPercent;
            return this;
        }

        public Builder manualBmrKcal(BigDecimal manualBmrKcal) {
            this.manualBmrKcal = manualBmrKcal;
            return this;
        }

        public Builder leanMassKg(BigDecimal leanMassKg) {
            this.leanMassKg = leanMassKg;
            return this;
        }

        public Builder muscleMassKg(BigDecimal muscleMassKg) {
            this.muscleMassKg = muscleMassKg;
            return this;
        }

        public Builder progressReviewIntervalDays(Integer progressReviewIntervalDays) {
            this.progressReviewIntervalDays = progressReviewIntervalDays;
            return this;
        }

        public Builder athleteModeEnabled(boolean athleteModeEnabled) {
            this.athleteModeEnabled = athleteModeEnabled;
            return this;
        }

        public Builder oneTimeCorrectionUsedAt(LocalDateTime oneTimeCorrectionUsedAt) {
            this.oneTimeCorrectionUsedAt = oneTimeCorrectionUsedAt;
            return this;
        }

        public Builder lastAthleteRegenAt(LocalDateTime lastAthleteRegenAt) {
            this.lastAthleteRegenAt = lastAthleteRegenAt;
            return this;
        }

        public Builder athleteRegenEligible(boolean athleteRegenEligible) {
            this.athleteRegenEligible = athleteRegenEligible;
            return this;
        }

        public Builder planRegenLockedUntil(LocalDate planRegenLockedUntil) {
            this.planRegenLockedUntil = planRegenLockedUntil;
            return this;
        }

        public Builder trainingDailyExtraKcal(BigDecimal trainingDailyExtraKcal) {
            this.trainingDailyExtraKcal = trainingDailyExtraKcal;
            return this;
        }

        public Builder bmrKcal(BigDecimal bmrKcal) {
            this.bmrKcal = bmrKcal;
            return this;
        }

        public Builder tdeeKcal(BigDecimal tdeeKcal) {
            this.tdeeKcal = tdeeKcal;
            return this;
        }

        public Builder targetCalories(BigDecimal targetCalories) {
            this.targetCalories = targetCalories;
            return this;
        }

        public Builder targetProteinG(BigDecimal targetProteinG) {
            this.targetProteinG = targetProteinG;
            return this;
        }

        public Builder targetCarbsG(BigDecimal targetCarbsG) {
            this.targetCarbsG = targetCarbsG;
            return this;
        }

        public Builder targetFatG(BigDecimal targetFatG) {
            this.targetFatG = targetFatG;
            return this;
        }

        public Builder paceWarning(String paceWarning) {
            this.paceWarning = paceWarning;
            return this;
        }

        public Builder estimatedWeeklyRateKg(BigDecimal estimatedWeeklyRateKg) {
            this.estimatedWeeklyRateKg = estimatedWeeklyRateKg;
            return this;
        }

        public Builder dailyWaterTargetMl(Integer dailyWaterTargetMl) {
            this.dailyWaterTargetMl = dailyWaterTargetMl;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder pregnancyStatus(PregnancyStatus pregnancyStatus) {
            this.pregnancyStatus = pregnancyStatus;
            return this;
        }

        public Builder eatingDisorderRisk(boolean eatingDisorderRisk) {
            this.eatingDisorderRisk = eatingDisorderRisk;
            return this;
        }

        public Builder severeRenalRestriction(boolean severeRenalRestriction) {
            this.severeRenalRestriction = severeRenalRestriction;
            return this;
        }

        public Builder aiPlanEligible(boolean aiPlanEligible) {
            this.aiPlanEligible = aiPlanEligible;
            return this;
        }

        public Builder hungerPattern(HungerPattern hungerPattern) {
            this.hungerPattern = hungerPattern;
            return this;
        }

        public Builder nutritionMode(NutritionMode nutritionMode) {
            this.nutritionMode = nutritionMode;
            return this;
        }

        public NutritionProfile build() {
            return new NutritionProfile(this);
        }
    }
}
