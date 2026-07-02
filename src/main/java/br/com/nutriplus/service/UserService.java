package br.com.nutriplus.service;

import br.com.nutriplus.application.auth.LoginUseCase;
import br.com.nutriplus.application.shared.ActingUserResolver;
import br.com.nutriplus.application.user.ChangePasswordUseCase;
import br.com.nutriplus.application.user.DeleteAccountUseCase;
import br.com.nutriplus.application.user.GetCurrentUserUseCase;
import br.com.nutriplus.application.user.UpdateUserProfileUseCase;
import br.com.nutriplus.domain.entity.UserLegalAcceptance;
import br.com.nutriplus.domain.enums.LegalDocumentType;
import br.com.nutriplus.dto.request.AcceptTermsRequest;
import br.com.nutriplus.dto.request.ChangePasswordRequest;
import br.com.nutriplus.dto.request.DeleteAccountRequest;
import br.com.nutriplus.dto.request.UpdateUserProfileRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.infrastructure.config.LegalProperties;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserLegalAcceptanceRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final ActingUserResolver actingUserResolver;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final ResponseMapper responseMapper;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final UserLegalAcceptanceRepository legalAcceptanceRepository;
    private final LegalProperties legalProperties;

    public UserService(ActingUserResolver actingUserResolver,
                       GetCurrentUserUseCase getCurrentUserUseCase,
                       UpdateUserProfileUseCase updateUserProfileUseCase,
                       ChangePasswordUseCase changePasswordUseCase,
                       DeleteAccountUseCase deleteAccountUseCase,
                       NutritionProfileRepository nutritionProfileRepository,
                       ResponseMapper responseMapper,
                       CurrentUser currentUser,
                       UserRepository userRepository,
                       UserLegalAcceptanceRepository legalAcceptanceRepository,
                       LegalProperties legalProperties) {
        this.actingUserResolver = actingUserResolver;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.responseMapper = responseMapper;
        this.currentUser = currentUser;
        this.userRepository = userRepository;
        this.legalAcceptanceRepository = legalAcceptanceRepository;
        this.legalProperties = legalProperties;
    }

    @Cacheable(value = NutriCacheNames.USER_ME, keyGenerator = "userIdCacheKeyGenerator")
    public UserResponse getMe() {
        var user = getCurrentUserUseCase.execute();
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.id()).isPresent();
        return responseMapper.toUserResponse(user, hasProfile);
    }

    @CacheEvict(value = NutriCacheNames.USER_ME, keyGenerator = "userIdCacheKeyGenerator")
    public UserResponse updateProfile(UpdateUserProfileRequest request) {
        Long userId = actingUserResolver.resolveUserId();
        var user = updateUserProfileUseCase.execute(userId, request);
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.id()).isPresent();
        return responseMapper.toUserResponse(user, hasProfile);
    }

    @CacheEvict(value = NutriCacheNames.USER_ME, keyGenerator = "userIdCacheKeyGenerator")
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
    @CacheEvict(value = NutriCacheNames.USER_ME, keyGenerator = "userIdCacheKeyGenerator")
    public UserResponse acceptTerms(AcceptTermsRequest request) {
        validateLegalVersions(request);
        if (!Boolean.TRUE.equals(request.healthEligibilityAccepted())) {
            throw new BusinessException("É necessário confirmar a elegibilidade para sugestões automáticas de plano.");
        }

        var entity = currentUser.get();
        LocalDateTime now = LocalDateTime.now();
        entity.setTermsAcceptedAt(now);
        entity.setTermsVersion(request.termsVersion());
        entity.setPrivacyPolicyAcceptedAt(now);
        entity.setPrivacyPolicyVersion(request.privacyVersion());
        entity.setHealthEligibilityAcceptedAt(now);
        entity.setHealthEligibilityVersion(request.healthEligibilityVersion());
        userRepository.save(entity);

        String platform = request.appPlatform() != null ? request.appPlatform() : "MOBILE";
        legalAcceptanceRepository.save(UserLegalAcceptance.record(
                entity, LegalDocumentType.TERMS, request.termsVersion(), platform, "ONBOARDING"));
        legalAcceptanceRepository.save(UserLegalAcceptance.record(
                entity, LegalDocumentType.PRIVACY, request.privacyVersion(), platform, "ONBOARDING"));
        legalAcceptanceRepository.save(UserLegalAcceptance.record(
                entity, LegalDocumentType.HEALTH_ELIGIBILITY, request.healthEligibilityVersion(), platform, "ONBOARDING"));

        boolean hasProfile = nutritionProfileRepository.findByUserId(entity.getId()).isPresent();
        return responseMapper.toUserResponse(entity, hasProfile);
    }

    @Transactional
    @CacheEvict(value = NutriCacheNames.USER_ME, keyGenerator = "userIdCacheKeyGenerator")
    public void deleteAccount(DeleteAccountRequest request) {
        var entity = currentUser.get();
        deleteAccountUseCase.execute(entity, request);
    }

    private void validateLegalVersions(AcceptTermsRequest request) {
        if (!legalProperties.version().equals(request.termsVersion())) {
            throw new BusinessException("Versão dos Termos de Uso desatualizada. Atualize o app e aceite novamente.");
        }
        if (!legalProperties.privacyVersion().equals(request.privacyVersion())) {
            throw new BusinessException("Versão da Política de Privacidade desatualizada. Atualize o app e aceite novamente.");
        }
        if (!legalProperties.healthEligibilityVersion().equals(request.healthEligibilityVersion())) {
            throw new BusinessException("Versão da declaração de elegibilidade desatualizada. Atualize o app e aceite novamente.");
        }
    }
}
