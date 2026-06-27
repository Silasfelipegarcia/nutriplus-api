package br.com.nutriplus.service;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.ServiceMode;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.util.ServiceModeCodec;
import br.com.nutriplus.dto.request.NutritionistRegisterRequest;
import br.com.nutriplus.dto.request.ProPricingUpdateRequest;
import br.com.nutriplus.dto.request.ProProfileUpdateRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.NutritionistPublicResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
public class NutritionistProService {

    private final UserRepository userRepository;
    private final NutritionistRepository nutritionistRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenPort tokenPort;
    private final UserQueryPort userQueryPort;
    private final ResponseMapper responseMapper;
    private final ProMapper proMapper;
    private final PricingGuidelineService pricingGuidelineService;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final CpfRegistrationService cpfRegistrationService;
    private final UserRegistrationValidator userRegistrationValidator;

    public NutritionistProService(UserRepository userRepository,
                                  NutritionistRepository nutritionistRepository,
                                  NutritionProfileRepository nutritionProfileRepository,
                                  PasswordHasherPort passwordHasherPort,
                                  TokenPort tokenPort,
                                  UserQueryPort userQueryPort,
                                  ResponseMapper responseMapper,
                                  ProMapper proMapper,
                                  PricingGuidelineService pricingGuidelineService,
                                  AuthorizationService authorizationService,
                                  AuditLogService auditLogService,
                                  CpfRegistrationService cpfRegistrationService,
                                  UserRegistrationValidator userRegistrationValidator) {
        this.userRepository = userRepository;
        this.nutritionistRepository = nutritionistRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenPort = tokenPort;
        this.userQueryPort = userQueryPort;
        this.responseMapper = responseMapper;
        this.proMapper = proMapper;
        this.pricingGuidelineService = pricingGuidelineService;
        this.authorizationService = authorizationService;
        this.auditLogService = auditLogService;
        this.cpfRegistrationService = cpfRegistrationService;
        this.userRegistrationValidator = userRegistrationValidator;
    }

    @Transactional
    public AuthResponse register(NutritionistRegisterRequest request) {
        userRegistrationValidator.validateNewNutritionistAccount(request.email(), request.cpf());
        var guidelines = pricingGuidelineService.requireGuidelines();

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordHasherPort.encode(request.password()))
                .role(UserRole.NUTRITIONIST)
                .build();
        cpfRegistrationService.applyCpf(user, request.cpf());
        user = userRepository.save(user);

        Nutritionist nutritionist = Nutritionist.createFor(
                user, request.crn(), request.bio(), request.specialties(),
                guidelines.getSuggestedPriceCents(), guidelines.getCareDurationDaysDefault());
        nutritionistRepository.save(nutritionist);

        auditLogService.log("NUTRITIONIST_REGISTER", "NUTRITIONIST", user);

        var domainUser = userQueryPort.findById(user.getId()).orElseThrow();
        return new AuthResponse(
                tokenPort.generateAccessToken(domainUser),
                tokenPort.generateRefreshToken(domainUser),
                "Bearer",
                tokenPort.accessExpirationSeconds(),
                responseMapper.toUserResponse(domainUser, false)
        );
    }

    public NutritionistPublicResponse getMyProfile() {
        return proMapper.toProfile(authorizationService.requireNutritionist());
    }

    @Transactional
    public NutritionistPublicResponse updateProfile(ProProfileUpdateRequest request) {
        Nutritionist n = authorizationService.requireNutritionist();
        if (request.bio() != null) {
            n.setBio(request.bio());
        }
        if (request.specialties() != null) {
            n.setSpecialties(request.specialties());
        }
        if (request.marketplaceVisible() != null) {
            if (Boolean.TRUE.equals(request.marketplaceVisible()) && !n.isCrnVerified()) {
                throw new BusinessException("CRN ainda não verificado. Marketplace indisponível até aprovação.");
            }
            n.setMarketplaceVisible(request.marketplaceVisible());
        }
        if (request.serviceModes() != null) {
            Set<ServiceMode> modes = EnumSet.copyOf(request.serviceModes());
            validateServiceModes(n, modes);
            n.setServiceModes(ServiceModeCodec.encode(modes));
        }
        if (request.city() != null) {
            n.setCity(request.city().isBlank() ? null : request.city().trim());
        }
        if (request.stateCode() != null) {
            n.setStateCode(request.stateCode().isBlank() ? null : request.stateCode().trim().toUpperCase());
        }
        if (request.neighborhood() != null) {
            n.setNeighborhood(request.neighborhood().isBlank() ? null : request.neighborhood().trim());
        }
        if (request.whatsappPhone() != null) {
            n.setWhatsappPhone(request.whatsappPhone().isBlank() ? null : request.whatsappPhone().trim());
        }
        validateServiceModes(n, ServiceModeCodec.decode(n.getServiceModes()));
        return proMapper.toProfile(nutritionistRepository.save(n));
    }

    @Transactional
    public NutritionistPublicResponse updatePricing(ProPricingUpdateRequest request) {
        Nutritionist n = authorizationService.requireNutritionist();
        pricingGuidelineService.validatePrice(request.consultationPriceCents());
        n.setConsultationPriceCents(request.consultationPriceCents());
        if (request.careDurationDays() != null) {
            n.setCareDurationDays(request.careDurationDays());
        }
        return proMapper.toProfile(nutritionistRepository.save(n));
    }

    private static void validateServiceModes(Nutritionist n, Set<ServiceMode> modes) {
        if (modes == null || modes.isEmpty()) {
            throw new BusinessException("Informe pelo menos um modo de atendimento.");
        }
        if (modes.contains(ServiceMode.IN_PERSON)) {
            if (n.getCity() == null || n.getCity().isBlank()
                    || n.getStateCode() == null || n.getStateCode().isBlank()) {
                throw new BusinessException("Atendimento presencial exige cidade e UF.");
            }
        }
    }
}
