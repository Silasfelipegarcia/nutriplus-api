package br.com.nutriplus.service;

import br.com.nutriplus.application.auth.LoginAccessPolicy;
import br.com.nutriplus.application.auth.LoginUseCase;
import br.com.nutriplus.application.auth.RefreshTokenUseCase;
import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.dto.request.LoginRequest;
import br.com.nutriplus.dto.request.RegisterRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.RegisterResponse;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenPort tokenPort;
    private final UserQueryPort userQueryPort;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final ResponseMapper responseMapper;
    private final AuditLogService auditLogService;
    private final CpfRegistrationService cpfRegistrationService;
    private final UserRegistrationValidator userRegistrationValidator;
    private final FeatureFlagService featureFlagService;

    public AuthService(UserRepository userRepository,
                       NutritionProfileRepository nutritionProfileRepository,
                       PasswordHasherPort passwordHasherPort,
                       TokenPort tokenPort,
                       UserQueryPort userQueryPort,
                       LoginUseCase loginUseCase,
                       RefreshTokenUseCase refreshTokenUseCase,
                       ResponseMapper responseMapper,
                       AuditLogService auditLogService,
                       CpfRegistrationService cpfRegistrationService,
                       UserRegistrationValidator userRegistrationValidator,
                       FeatureFlagService featureFlagService) {
        this.userRepository = userRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenPort = tokenPort;
        this.userQueryPort = userQueryPort;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.responseMapper = responseMapper;
        this.auditLogService = auditLogService;
        this.cpfRegistrationService = cpfRegistrationService;
        this.userRegistrationValidator = userRegistrationValidator;
        this.featureFlagService = featureFlagService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!featureFlagService.isEnabled("REGISTRATION_OPEN")) {
            throw new br.com.nutriplus.exception.BusinessException("Cadastros temporariamente fechados.");
        }
        User user = createPendingPatient(request, RegistrationSource.OPEN);
        auditLogService.log("REGISTER", "USER", user);
        return toRegisterResponse(user, LoginAccessPolicy.PENDING_MESSAGE);
    }

    @Transactional
    public RegisterResponse betaRequest(RegisterRequest request) {
        User user = createPendingPatient(request, RegistrationSource.BETA_WAITLIST);
        auditLogService.log("BETA_REQUEST", "USER", user);
        return toRegisterResponse(user, LoginAccessPolicy.BETA_WAITLIST_MESSAGE);
    }

    private User createPendingPatient(RegisterRequest request, RegistrationSource source) {
        userRegistrationValidator.validateNewPatientAccount(
                request.email(), request.cpf(), request.birthDate());

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordHasherPort.encode(request.password()))
                .loginEnabled(false)
                .registrationSource(source)
                .acquisitionSource(trimToNull(request.acquisitionSource()))
                .acquisitionMedium(trimToNull(request.acquisitionMedium()))
                .acquisitionCampaign(trimToNull(request.acquisitionCampaign()))
                .acquisitionLanding(trimToNull(request.acquisitionLanding()))
                .build();
        cpfRegistrationService.applyCpf(user, request.cpf());
        return userRepository.save(user);
    }

    private RegisterResponse toRegisterResponse(User user, String message) {
        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                false,
                message
        );
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public AuthResponse login(LoginRequest request) {
        LoginUseCase.Response result = loginUseCase.execute(
                new LoginUseCase.Request(request.email(), request.password()));
        auditLogService.log("LOGIN_SUCCESS", "USER", toEntity(result.user()));
        return fromLoginResponse(result);
    }

    public AuthResponse refresh(String refreshToken) {
        LoginUseCase.Response result = refreshTokenUseCase.execute(refreshToken);
        auditLogService.log("TOKEN_REFRESH", "USER", toEntity(result.user()));
        return fromLoginResponse(result);
    }

    private AuthResponse toAuthResponse(br.com.nutriplus.domain.model.User user) {
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.id()).isPresent();
        return new AuthResponse(
                tokenPort.generateAccessToken(user),
                tokenPort.generateRefreshToken(user),
                "Bearer",
                tokenPort.accessExpirationSeconds(),
                responseMapper.toUserResponse(user, hasProfile)
        );
    }

    private AuthResponse fromLoginResponse(LoginUseCase.Response result) {
        boolean hasProfile = nutritionProfileRepository.findByUserId(result.user().id()).isPresent();
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresInSeconds(),
                responseMapper.toUserResponse(result.user(), hasProfile)
        );
    }

    private User toEntity(br.com.nutriplus.domain.model.User user) {
        return userRepository.findById(user.id()).orElse(null);
    }
}
