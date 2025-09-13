package br.com.nutriplus.domain.port.in;

import br.com.nutriplus.domain.model.UserProfile;

import java.util.UUID;

public interface OnboardingUseCase {
    UUID onboard(UserProfile userProfile, String goalType, String pace);
}