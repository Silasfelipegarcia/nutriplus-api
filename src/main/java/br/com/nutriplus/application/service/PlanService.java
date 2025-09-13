package br.com.nutriplus.application.service;

import br.com.nutriplus.domain.model.Plan;
import br.com.nutriplus.domain.model.PlanDay;
import br.com.nutriplus.domain.model.UserProfile;
import br.com.nutriplus.domain.port.in.GeneratePlanUseCase;
import br.com.nutriplus.domain.port.in.GetCurrentPlanQuery;
import br.com.nutriplus.domain.port.out.LLMPort;
import br.com.nutriplus.domain.port.out.PlanRepository;
import br.com.nutriplus.domain.port.out.UserProfileRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PlanService implements GeneratePlanUseCase, GetCurrentPlanQuery {

    private final PlanRepository planRepository;
    private final UserProfileRepository userProfileRepository;
    private final LLMPort llmPort;

    public PlanService(PlanRepository planRepository,
                       UserProfileRepository userProfileRepository,
                       @Qualifier("llmPort") LLMPort llmPort) { // <-- seleciona o bean certo
        this.planRepository = planRepository;
        this.userProfileRepository = userProfileRepository;
        this.llmPort = llmPort;
    }

    @Override
    public int generate(UUID userId) {
        UserProfile userProfile = userProfileRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        int nextVersion = Math.max(1, planRepository.latestVersionNumber(userId) + 1);
        Plan planHeader = new Plan(UUID.randomUUID(), userId, nextVersion, LocalDate.now(), null);
        Plan savedPlan = planRepository.savePlanHeader(planHeader);
        double basalMetabolicRate = "M".equalsIgnoreCase(userProfile.getSex()) ?
                10 * userProfile.getWeightKg() + 6.25 * userProfile.getHeightCm() - 5 * userProfile.getAge() + 5 :
                10 * userProfile.getWeightKg() + 6.25 * userProfile.getHeightCm() - 5 * userProfile.getAge() - 161;
        double activityFactor = userProfile.getTrainingDaysPerWeek() <= 1 ? 1.2 : (userProfile.getTrainingDaysPerWeek() <= 4 ? 1.4 : 1.55);
        double totalDailyEnergyExpenditure = basalMetabolicRate * activityFactor;
        double dailyCalories = totalDailyEnergyExpenditure * 0.85;
        double dailyProteinGrams = Math.round((1.8 * userProfile.getWeightKg()) / 5.0) * 5.0;
        double dailyFatGrams = Math.round((1.0 * userProfile.getWeightKg()) / 5.0) * 5.0;
        double dailyCarbGrams = Math.round(((dailyCalories - (dailyProteinGrams * 4 + dailyFatGrams * 9)) / 4) / 5.0) * 5.0;
        Map<String, Object> mealsTemplate = new LinkedHashMap<>();
        mealsTemplate.put("calories", dailyCalories);
        mealsTemplate.put("protein_g", dailyProteinGrams);
        mealsTemplate.put("carbs_g", dailyCarbGrams);
        mealsTemplate.put("fats_g", dailyFatGrams);
        mealsTemplate.put("breakfast", List.of(Map.of("food", "ovos", "qtd", "3 un"), Map.of("food", "pão integral", "qtd", "2 fatias"), Map.of("food", "whey", "qtd", "1 scoop"), Map.of("food", "café", "qtd", "1 xíc")));
        mealsTemplate.put("lunch", List.of(Map.of("food", "arroz", "qtd", "4 cs"), Map.of("food", "feijão", "qtd", "1 concha"), Map.of("food", "frango", "qtd", "200 g"), Map.of("food", "ovo cozido", "qtd", "1 un")));
        mealsTemplate.put("dinner", List.of(Map.of("food", "arroz", "qtd", "2 cs"), Map.of("food", "feijão", "qtd", "1/2 concha"), Map.of("food", "carne magra", "qtd", "150 g")));
        String llmDailySummary = llmPort.summarizeDaily();
        if (llmDailySummary == null || llmDailySummary.isBlank()) {
            llmDailySummary = "Foco do dia: proteína em todas as refeições, 2L de água e treino de 40–50 minutos.";
        }
        List<PlanDay> planDays = new ArrayList<>();
        for (int dayIndex = 1; dayIndex <= 15; dayIndex++) {
            PlanDay planDay = new PlanDay(UUID.randomUUID(), savedPlan.getId(), dayIndex, mealsTemplate, llmDailySummary + " Dia " + dayIndex + ".");
            planDays.add(planDay);
        }
        planRepository.saveDays(planDays);
        return nextVersion;
    }

    @Override
    public Plan get(UUID userId) {
        return planRepository.findLatestPlan(userId).orElse(new Plan(UUID.randomUUID(), userId, 0, LocalDate.now(), List.of()));
    }

}