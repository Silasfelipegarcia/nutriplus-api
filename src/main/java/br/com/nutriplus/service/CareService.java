package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.CareRelationshipSource;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.ConsultationStatus;
import br.com.nutriplus.domain.enums.PreferredCareMode;
import br.com.nutriplus.domain.enums.ServiceMode;
import br.com.nutriplus.domain.util.ServiceModeCodec;
import br.com.nutriplus.dto.request.AcceptInviteRequest;
import br.com.nutriplus.dto.request.CareRequestRequest;
import br.com.nutriplus.dto.request.CreateInviteRequest;
import br.com.nutriplus.dto.response.CareContactResponse;
import br.com.nutriplus.dto.response.CareRelationshipResponse;
import br.com.nutriplus.dto.response.InviteResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.repository.*;
import br.com.nutriplus.security.AuthorizationService;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class CareService {

    private final CurrentUser currentUser;
    private final AuthorizationService authorizationService;
    private final NutritionistRepository nutritionistRepository;
    private final CareRelationshipRepository careRelationshipRepository;
    private final NutritionistInviteRepository inviteRepository;
    private final PatientDataConsentRepository consentRepository;
    private final ConversationThreadRepository threadRepository;
    private final ProMapper proMapper;

    @Value("${nutriplus.pro.invite-base-url:http://localhost:3000}")
    private String inviteBaseUrl;

    public CareService(CurrentUser currentUser,
                       AuthorizationService authorizationService,
                       NutritionistRepository nutritionistRepository,
                       CareRelationshipRepository careRelationshipRepository,
                       NutritionistInviteRepository inviteRepository,
                       PatientDataConsentRepository consentRepository,
                       ConversationThreadRepository threadRepository,
                       ProMapper proMapper) {
        this.currentUser = currentUser;
        this.authorizationService = authorizationService;
        this.nutritionistRepository = nutritionistRepository;
        this.careRelationshipRepository = careRelationshipRepository;
        this.inviteRepository = inviteRepository;
        this.consentRepository = consentRepository;
        this.threadRepository = threadRepository;
        this.proMapper = proMapper;
    }

    public List<Nutritionist> listMarketplace(ServiceMode mode, String state, String city) {
        String normalizedState = normalizeState(state);
        String normalizedCity = normalizeCity(city);

        Comparator<Nutritionist> comparator = Comparator.comparing(Nutritionist::getConsultationPriceCents);
        if (mode == ServiceMode.ONLINE) {
            comparator = Comparator.<Nutritionist>comparingInt(n -> ServiceModeCodec.includes(n.getServiceModes(), ServiceMode.ONLINE) ? 0 : 1)
                    .thenComparing(comparator);
        }

        return nutritionistRepository.findByMarketplaceVisibleTrueAndCrnVerifiedTrueOrderByCreatedAtDesc()
                .stream()
                .filter(n -> matchesMode(n, mode))
                .filter(n -> matchesState(n, normalizedState))
                .filter(n -> matchesCity(n, normalizedCity))
                .sorted(comparator)
                .toList();
    }

    public Nutritionist getMarketplaceNutritionist(Long id) {
        Nutritionist n = nutritionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista não encontrado"));
        if (!n.isMarketplaceVisible() || !n.isCrnVerified()) {
            throw new ResourceNotFoundException("Nutricionista não disponível");
        }
        return n;
    }

    @Transactional
    public InviteResponse createInvite(CreateInviteRequest request) {
        Nutritionist nutritionist = authorizationService.requireNutritionist();
        NutritionistInvite invite = NutritionistInvite.create(nutritionist, generateCode());
        invite.setMaxUses(request.maxUses());
        if (request.expiresInDays() != null) {
            invite.setExpiresAt(LocalDateTime.now().plusDays(request.expiresInDays()));
        }
        invite = inviteRepository.save(invite);
        return proMapper.toInvite(invite, inviteBaseUrl);
    }

    @Transactional
    public CareRelationshipResponse acceptInvite(String code, AcceptInviteRequest request) {
        if (!Boolean.TRUE.equals(request.consentDataSharing())) {
            throw new BusinessException("É necessário consentir o compartilhamento de dados.");
        }
        User patient = currentUser.get();
        NutritionistInvite invite = inviteRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Convite inválido"));
        if (!invite.isValid()) {
            throw new BusinessException("Convite expirado ou esgotado.");
        }

        Nutritionist nutritionist = invite.getNutritionist();
        CareRelationship care = careRelationshipRepository
                .findByPatientIdAndNutritionistId(patient.getId(), nutritionist.getId())
                .orElseGet(() -> {
                    CareRelationship cr = CareRelationship.create(patient, nutritionist, CareRelationshipSource.INVITE);
                    return careRelationshipRepository.save(cr);
                });

        if (care.getStatus() == CareRelationshipStatus.CANCELLED) {
            care.setStatus(CareRelationshipStatus.PRE_ENGAGED);
        }

        if (consentRepository.findByCareRelationshipId(care.getId()).isEmpty()) {
            consentRepository.save(PatientDataConsent.grant(patient, nutritionist, care));
        }

        invite.incrementUseCount();
        inviteRepository.save(invite);

        return proMapper.toCare(care);
    }

    @Transactional
    public CareRelationshipResponse requestMarketplaceCare(Long nutritionistId, CareRequestRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.consentDataSharing())) {
            throw new BusinessException("É necessário consentir o compartilhamento de dados.");
        }
        authorizationService.requirePatient();
        User patient = currentUser.get();
        Nutritionist nutritionist = getMarketplaceNutritionist(nutritionistId);

        PreferredCareMode mode = request.preferredCareMode() != null ? request.preferredCareMode() : PreferredCareMode.EITHER;
        validatePreferredMode(nutritionist, mode);

        CareRelationship care = careRelationshipRepository
                .findByPatientIdAndNutritionistId(patient.getId(), nutritionist.getId())
                .orElseGet(() -> careRelationshipRepository.save(
                        CareRelationship.create(patient, nutritionist, CareRelationshipSource.MARKETPLACE)));

        if (care.getStatus() == CareRelationshipStatus.ACTIVE) {
            throw new BusinessException("Você já possui acompanhamento ativo com este nutricionista.");
        }
        care.setStatus(CareRelationshipStatus.PENDING_PAYMENT);
        care.setPreferredCareMode(mode);
        care = careRelationshipRepository.save(care);

        if (consentRepository.findByCareRelationshipId(care.getId()).isEmpty()) {
            consentRepository.save(PatientDataConsent.grant(patient, nutritionist, care));
        }

        return proMapper.toCare(care);
    }

    public CareContactResponse getCareContact(Long careRelationshipId) {
        User patient = authorizationService.requireAuthenticated();
        CareRelationship care = careRelationshipRepository.findById(careRelationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado."));
        if (!care.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException("Sem permissão para este vínculo.");
        }
        if (care.getStatus() != CareRelationshipStatus.ACTIVE) {
            throw new BusinessException("Contato disponível apenas com acompanhamento ativo.");
        }
        String phone = care.getNutritionist().getWhatsappPhone();
        if (phone == null || phone.isBlank()) {
            return new CareContactResponse(null, null);
        }
        String digits = phone.replaceAll("\\D", "");
        return new CareContactResponse(phone, "https://wa.me/" + digits);
    }

    public List<CareRelationshipResponse> listMyCareAsPatient() {
        User patient = currentUser.get();
        return careRelationshipRepository.findByPatientIdOrderByUpdatedAtDesc(patient.getId())
                .stream().map(proMapper::toCare).toList();
    }

    public List<CareRelationshipResponse> listCaseload() {
        Nutritionist nutritionist = authorizationService.requireNutritionist();
        return careRelationshipRepository.findByNutritionistIdOrderByUpdatedAtDesc(nutritionist.getId())
                .stream().map(proMapper::toCare).toList();
    }

    @Transactional
    public void activateCareAfterPayment(CareRelationship care, Consultation consultation) {
        care.setStatus(CareRelationshipStatus.ACTIVE);
        care.setStartedAt(LocalDateTime.now());
        care.setExpiresAt(LocalDateTime.now().plusDays(care.getNutritionist().getCareDurationDays()));
        careRelationshipRepository.save(care);

        consultation.setStatus(ConsultationStatus.PAID);
        consultation.setPaidAt(LocalDateTime.now());

        if (threadRepository.findByCareRelationshipId(care.getId()).isEmpty()) {
            threadRepository.save(ConversationThread.forCare(care));
        }

        if (consentRepository.findByCareRelationshipId(care.getId()).isEmpty()) {
            consentRepository.save(PatientDataConsent.grant(
                    care.getPatient(), care.getNutritionist(), care));
        }
    }

    static void validatePreferredMode(Nutritionist nutritionist, PreferredCareMode preferred) {
        if (preferred == PreferredCareMode.ONLINE
                && !ServiceModeCodec.includes(nutritionist.getServiceModes(), ServiceMode.ONLINE)) {
            throw new BusinessException("Este nutricionista não atende online.");
        }
        if (preferred == PreferredCareMode.IN_PERSON
                && !ServiceModeCodec.includes(nutritionist.getServiceModes(), ServiceMode.IN_PERSON)) {
            throw new BusinessException("Este nutricionista não atende presencialmente.");
        }
    }

    private static boolean matchesMode(Nutritionist n, ServiceMode mode) {
        if (mode == null) {
            return true;
        }
        return ServiceModeCodec.includes(n.getServiceModes(), mode);
    }

    private static boolean matchesState(Nutritionist n, String state) {
        if (state == null) {
            return true;
        }
        return state.equalsIgnoreCase(n.getStateCode());
    }

    private static boolean matchesCity(Nutritionist n, String city) {
        if (city == null) {
            return true;
        }
        return n.getCity() != null && n.getCity().toLowerCase(Locale.ROOT).contains(city);
    }

    private static String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }
        return state.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeCity(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }
        return city.trim().toLowerCase(Locale.ROOT);
    }

    private String generateCode() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
