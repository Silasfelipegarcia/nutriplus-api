package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.entity.UserTrainingActivity;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.domain.enums.Sex;
import br.com.nutriplus.domain.enums.SportType;
import br.com.nutriplus.domain.enums.ActivityLevel;
import br.com.nutriplus.domain.enums.DietaryPreference;
import br.com.nutriplus.domain.enums.Restriction;
import br.com.nutriplus.domain.enums.AgentPersona;
import br.com.nutriplus.dto.request.TrainingActivityRequest;
import br.com.nutriplus.dto.request.TrainingProfileRequest;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserTrainingActivityRepository;
import br.com.nutriplus.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class TrainingServiceMacrosTest {

    @Mock
    private CurrentUser currentUser;
    @Mock
    private NutritionProfileRepository nutritionProfileRepository;
    @Mock
    private UserTrainingActivityRepository activityRepository;
    @Mock
    private AiAgentClient aiAgentClient;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private CoachInsightService coachInsightService;

    @InjectMocks
    private TrainingService trainingService;

    private User user;
    private NutritionProfile profile;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("a@test.com").build();
        profile = NutritionProfile.builder()
                .user(user)
                .age(30)
                .sex(Sex.MALE)
                .heightCm(new BigDecimal("175"))
                .currentWeightKg(new BigDecimal("80"))
                .targetWeightKg(new BigDecimal("75"))
                .goal(Goal.LOSE_WEIGHT)
                .activityLevel(ActivityLevel.MODERATE)
                .dietaryPreference(DietaryPreference.OMNIVORE)
                .restriction(Restriction.NONE)
                .agentPersona(AgentPersona.LUNA)
                .targetCalories(new BigDecimal("1800"))
                .build();

        when(currentUser.get()).thenReturn(user);
        when(nutritionProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(coachInsightService.trainingReview(any(), any())).thenReturn(null);
    }

    @Test
    void saveProfileWithAthleteModeRecalculatesMacrosWithTrainingExtra() {
        UserTrainingActivity savedActivity = new UserTrainingActivity(
                user, SportType.WEIGHT_TRAINING, 3, 60, null);
        when(activityRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(savedActivity));
        when(aiAgentClient.calculateMacros(any())).thenReturn(
                new AiNutritionCalculateResponse(
                        new BigDecimal("1700"),
                        new BigDecimal("2200"),
                        new BigDecimal("2100"),
                        new BigDecimal("140"),
                        new BigDecimal("200"),
                        new BigDecimal("60"),
                        "ESTIMATE",
                        null
                ));

        TrainingProfileRequest request = new TrainingProfileRequest(
                true,
                List.of(new TrainingActivityRequest("WEIGHT_TRAINING", 3, 60, null))
        );

        var response = trainingService.saveProfile(request);

        ArgumentCaptor<NutritionProfile> captor = ArgumentCaptor.forClass(NutritionProfile.class);
        verify(nutritionProfileRepository, atLeast(1)).save(captor.capture());
        NutritionProfile saved = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertTrue(saved.isAthleteModeEnabled());
        assertTrue(saved.getTrainingDailyExtraKcal().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(new BigDecimal("2100"), saved.getTargetCalories());
        assertTrue(response.appliedToPlan());
    }

    @Test
    void saveProfileDisablingAthleteModeRecalculatesWithoutTrainingExtra() {
        profile.setAthleteModeEnabled(true);
        profile.setTrainingDailyExtraKcal(new BigDecimal("250"));

        when(activityRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of());
        when(aiAgentClient.calculateMacros(any())).thenReturn(
                new AiNutritionCalculateResponse(
                        new BigDecimal("1700"),
                        new BigDecimal("2200"),
                        new BigDecimal("1700"),
                        new BigDecimal("130"),
                        new BigDecimal("170"),
                        new BigDecimal("55"),
                        "ESTIMATE",
                        null
                ));

        TrainingProfileRequest request = new TrainingProfileRequest(false, List.of());

        trainingService.saveProfile(request);

        ArgumentCaptor<NutritionProfile> captor = ArgumentCaptor.forClass(NutritionProfile.class);
        verify(nutritionProfileRepository, atLeast(1)).save(captor.capture());
        NutritionProfile saved = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertNull(saved.getTrainingDailyExtraKcal());
        assertEquals(new BigDecimal("1700"), saved.getTargetCalories());
    }

    @Test
    void syncTrainingDailyExtraSetsKcalFromActivities() {
        profile.setAthleteModeEnabled(true);
        UserTrainingActivity activity = new UserTrainingActivity(
                user, SportType.WEIGHT_TRAINING, 3, 60, null);
        when(activityRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(activity));

        trainingService.syncTrainingDailyExtra(profile);

        assertTrue(profile.getTrainingDailyExtraKcal().compareTo(BigDecimal.ZERO) > 0);
    }
}
