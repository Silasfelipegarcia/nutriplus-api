package br.com.nutriplus.service;

import br.com.nutriplus.application.auth.LoginUseCase;
import br.com.nutriplus.application.shared.ActingUserResolver;
import br.com.nutriplus.application.user.ChangePasswordUseCase;
import br.com.nutriplus.application.user.GetCurrentUserUseCase;
import br.com.nutriplus.application.user.UpdateUserProfileUseCase;
import br.com.nutriplus.dto.request.ChangePasswordRequest;
import br.com.nutriplus.dto.request.UpdateUserProfileRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final ActingUserResolver actingUserResolver;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final ResponseMapper responseMapper;

    public UserService(ActingUserResolver actingUserResolver,
                       GetCurrentUserUseCase getCurrentUserUseCase,
                       UpdateUserProfileUseCase updateUserProfileUseCase,
                       ChangePasswordUseCase changePasswordUseCase,
                       NutritionProfileRepository nutritionProfileRepository,
                       ResponseMapper responseMapper) {
        this.actingUserResolver = actingUserResolver;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.responseMapper = responseMapper;
    }

    public UserResponse getMe() {
        var user = getCurrentUserUseCase.execute();
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.id()).isPresent();
        return responseMapper.toUserResponse(user, hasProfile);
    }

    public UserResponse updateProfile(UpdateUserProfileRequest request) {
        Long userId = actingUserResolver.resolveUserId();
        var user = updateUserProfileUseCase.execute(userId, request);
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.id()).isPresent();
        return responseMapper.toUserResponse(user, hasProfile);
    }

    public AuthResponse changePassword(ChangePasswordRequest request) {
        Long userId = actingUserResolver.resolveUserId();
        LoginUseCase.Response result = changePasswordUseCase.execute(userId, request);
        boolean hasProfile = nutritionProfileRepository.findByUserId(userId).isPresent();
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresInSeconds(),
                responseMapper.toUserResponse(result.user(), hasProfile)
        );
    }
}
