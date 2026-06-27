package br.com.nutriplus.security;

import br.com.nutriplus.domain.entity.CareRelationship;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.PatientDataConsentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorizationService {

    private final CurrentUser currentUser;
    private final NutritionistRepository nutritionistRepository;
    private final CareRelationshipRepository careRelationshipRepository;
    private final PatientDataConsentRepository consentRepository;

    public AuthorizationService(CurrentUser currentUser,
                                NutritionistRepository nutritionistRepository,
                                CareRelationshipRepository careRelationshipRepository,
                                PatientDataConsentRepository consentRepository) {
        this.currentUser = currentUser;
        this.nutritionistRepository = nutritionistRepository;
        this.careRelationshipRepository = careRelationshipRepository;
        this.consentRepository = consentRepository;
    }

    public User requireAuthenticated() {
        return currentUser.get();
    }

    public Nutritionist requireNutritionist() {
        User user = requireAuthenticated();
        if (user.getRole() != UserRole.NUTRITIONIST) {
            throw new BusinessException("Acesso restrito a nutricionistas.");
        }
        return nutritionistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Perfil de nutricionista não encontrado."));
    }

    public void requirePatient() {
        User user = requireAuthenticated();
        if (user.getRole() != UserRole.PATIENT && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Acesso restrito a pacientes.");
        }
    }

    public CareRelationship requireCareAccessForNutritionist(Long careRelationshipId) {
        Nutritionist nutritionist = requireNutritionist();
        CareRelationship care = careRelationshipRepository.findById(careRelationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado."));
        if (!care.getNutritionist().getId().equals(nutritionist.getId())) {
            throw new BusinessException("Sem permissão para este paciente.");
        }
        if (!care.allowsNutritionistAccess()) {
            throw new BusinessException("Vínculo não permite acesso aos dados.");
        }
        consentRepository.findByCareRelationshipId(care.getId())
                .orElseThrow(() -> new BusinessException("Consentimento do paciente não registrado."));
        return care;
    }

    public CareRelationship requireCareAccessForNutritionistByPatientId(Long patientId) {
        Nutritionist nutritionist = requireNutritionist();
        CareRelationship care = careRelationshipRepository
                .findByPatientIdAndNutritionistId(patientId, nutritionist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não vinculado."));
        if (!care.allowsNutritionistAccess()) {
            throw new BusinessException("Vínculo não permite acesso aos dados.");
        }
        consentRepository.findByCareRelationshipId(care.getId())
                .orElseThrow(() -> new BusinessException("Consentimento do paciente não registrado."));
        return care;
    }

    public CareRelationship requireActiveCareForPatient(Long nutritionistId) {
        User patient = requireAuthenticated();
        CareRelationship care = careRelationshipRepository
                .findByPatientIdAndNutritionistId(patient.getId(), nutritionistId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado."));
        return care;
    }

    public List<String> currentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return List.of();
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null ? roles : List.of();
    }

    public boolean hasRole(UserRole role) {
        return currentRoles().contains(role.name());
    }

    public Long currentUserId() {
        return requireAuthenticated().getId();
    }
}
