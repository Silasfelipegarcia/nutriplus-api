package br.com.nutriplus.application.service;

import br.com.nutriplus.domain.model.Goal;
import br.com.nutriplus.domain.model.UserProfile;
import br.com.nutriplus.domain.port.in.GeneratePlanUseCase;
import br.com.nutriplus.domain.port.in.OnboardingUseCase;
import br.com.nutriplus.domain.port.out.GoalRepository;
import br.com.nutriplus.domain.port.out.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class OnboardingService implements OnboardingUseCase {
    private final UserProfileRepository userProfileRepository;
    private final GoalRepository goalRepository;
    private final GeneratePlanUseCase generatePlanUseCase;

    public OnboardingService(UserProfileRepository userProfileRepository, GoalRepository goalRepository, GeneratePlanUseCase generatePlanUseCase) {
        this.userProfileRepository = userProfileRepository;
        this.goalRepository = goalRepository;
        this.generatePlanUseCase = generatePlanUseCase;
    }

    @Override
    public UUID onboard(UserProfile userProfile, String goalType, String pace) {
        UserProfile savedUser = userProfileRepository.save(userProfile);
        Goal goal = new Goal(UUID.randomUUID(), savedUser.getId(), goalType, pace, null, null, OffsetDateTime.now());
        goalRepository.save(goal);
        generatePlanUseCase.generate(savedUser.getId());
        return savedUser.getId();
    }
}