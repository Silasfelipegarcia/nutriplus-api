package br.com.nutriplus.service;

import br.com.nutriplus.application.auth.LoginUseCase;
import br.com.nutriplus.application.shared.ActingUserResolver;
import br.com.nutriplus.application.user.ChangePasswordUseCase;
import br.com.nutriplus.application.user.GetCurrentUserUseCase;
import br.com.nutriplus.application.user.UpdateUserProfileUseCase;
import br.com.nutriplus.dto.request.AcceptTermsRequest;
import br.com.nutriplus.dto.request.ChangePasswordRequest;
import br.com.nutriplus.dto.request.UpdateUserProfileRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final ActingUserResolver actingUserResolver;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final ResponseMapper responseMapper;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public UserService(ActingUserResolver actingUserResolver,
                       GetCurrentUserUseCase getCurrentUserUseCase,
                       UpdateUserProfileUseCase updateUserProfileUseCase,
                       ChangePasswordUseCase changePasswordUseCase,
                       NutritionProfileRepository nutritionProfileRepository,
                       ResponseMapper responseMapper,
                       CurrentUser currentUser,
                       UserRepository userRepository) {
        this.actingUserResolver = actingUserResolver;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.responseMapper = responseMapper;
        this.currentUser = currentUser;
        this.userRepository = userRepository;
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

    @Transactional
    public UserResponse acceptTerms(AcceptTermsRequest request) {
        var entity = currentUser.get();
        LocalDateTime now = LocalDateTime.now();
        entity.setTermsAcceptedAt(now);
        entity.setTermsVersion(request.termsVersion());
        entity.setPrivacyPolicyAcceptedAt(now);
        userRepository.save(entity);
        boolean hasProfile = nutritionProfileRepository.findByUserId(entity.getId()).isPresent();
        return responseMapper.toUserResponse(entity, hasProfile);
    }

    @Transactional
    public void deleteAccount() {
        var entity = currentUser.get();
        userRepository.delete(entity);
    }
}
