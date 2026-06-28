package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.RejectUserAccessRequest;
import br.com.nutriplus.dto.request.UpdateLoginEnabledRequest;
import br.com.nutriplus.dto.request.UpdateUserAdminRequest;
import br.com.nutriplus.dto.response.AdminAccessSummaryResponse;
import br.com.nutriplus.dto.response.AdminUserAccessResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAccessService {

    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final NutritionistRepository nutritionistRepository;
    private final BetaAccessNotificationService betaAccessNotificationService;

    public AdminAccessService(AuthorizationService authorizationService,
                              UserRepository userRepository,
                              NutritionProfileRepository nutritionProfileRepository,
                              NutritionistRepository nutritionistRepository,
                              BetaAccessNotificationService betaAccessNotificationService) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.nutritionistRepository = nutritionistRepository;
        this.betaAccessNotificationService = betaAccessNotificationService;
    }

    public AdminAccessSummaryResponse summary() {
        requireAdmin();
        long pending = userRepository.countByLoginEnabledFalseAndAccessRejectedAtIsNull();
        long enabled = userRepository.countByLoginEnabledTrue();
        long admins = userRepository.countByRole(UserRole.ADMIN);
        long pendingNutritionists = nutritionistRepository.countByCrnVerifiedFalse();
        return new AdminAccessSummaryResponse(pending, enabled, admins, pendingNutritionists, userRepository.count());
    }

    public List<AdminUserAccessResponse> listAdmins() {
        requireAdmin();
        return userRepository.findByRoleOrderByCreatedAtDesc(UserRole.ADMIN).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AdminUserAccessResponse> listPending() {
        requireAdmin();
        return userRepository.findByLoginEnabledFalseAndAccessRejectedAtIsNullOrderByCreatedAtAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AdminUserAccessResponse> listApproved() {
        requireAdmin();
        return userRepository.findByLoginEnabledTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminUserAccessResponse setLoginEnabled(Long userId, UpdateLoginEnabledRequest request) {
        requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (user.getRole() == UserRole.ADMIN && !request.enabled()) {
            throw new BusinessException("Não é possível desabilitar o login de um administrador.");
        }

        boolean enabled = request.enabled();
        boolean wasEnabled = user.isLoginEnabled();
        user.setLoginEnabled(enabled);
        if (enabled) {
            user.setLoginEnabledAt(LocalDateTime.now());
            user.setLoginEnabledBy(authorizationService.currentUserId());
            clearAccessRejection(user);
            if (user.getRole() == UserRole.NUTRITIONIST) {
                nutritionistRepository.findByUserId(user.getId()).ifPresent(n -> {
                    n.setCrnVerified(true);
                    nutritionistRepository.save(n);
                });
            }
        } else {
            user.setLoginEnabledAt(null);
            user.setLoginEnabledBy(null);
        }
        userRepository.save(user);
        if (enabled && !wasEnabled) {
            betaAccessNotificationService.notifyApproved(user);
        }
        return toResponse(user);
    }

    @Transactional
    public AdminUserAccessResponse rejectAccess(Long userId, RejectUserAccessRequest request) {
        requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException("Não é possível recusar o acesso de um administrador.");
        }
        if (user.isLoginEnabled()) {
            throw new BusinessException("Este usuário já possui login liberado.");
        }
        if (user.isAccessRejected()) {
            throw new BusinessException("Este cadastro já foi recusado.");
        }

        String reason = request != null ? request.reason() : null;
        user.setAccessRejectedAt(LocalDateTime.now());
        user.setAccessRejectedBy(authorizationService.currentUserId());
        user.setAccessRejectionReason(normalizeReason(reason));
        userRepository.save(user);
        betaAccessNotificationService.notifyRejected(user, reason);
        return toResponse(user);
    }

    @Transactional
    public AdminUserAccessResponse setUserAdmin(Long userId, UpdateUserAdminRequest request) {
        requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        Long currentUserId = authorizationService.currentUserId();

        if (request.admin()) {
            if (user.getRole() == UserRole.ADMIN) {
                return toResponse(user);
            }
            user.setRole(UserRole.ADMIN);
            user.setLoginEnabled(true);
            user.setLoginEnabledAt(LocalDateTime.now());
            user.setLoginEnabledBy(currentUserId);
        } else {
            if (user.getRole() != UserRole.ADMIN) {
                return toResponse(user);
            }
            if (user.getId().equals(currentUserId)) {
                throw new BusinessException("Você não pode remover seu próprio acesso de administrador.");
            }
            if (userRepository.countByRole(UserRole.ADMIN) <= 1) {
                throw new BusinessException("É necessário manter pelo menos um administrador.");
            }
            user.setRole(resolveRoleAfterAdminRemoval(user));
        }

        userRepository.save(user);
        return toResponse(user);
    }

    private UserRole resolveRoleAfterAdminRemoval(User user) {
        if (nutritionistRepository.findByUserId(user.getId()).isPresent()) {
            return UserRole.NUTRITIONIST;
        }
        return UserRole.PATIENT;
    }

    private void clearAccessRejection(User user) {
        user.setAccessRejectedAt(null);
        user.setAccessRejectedBy(null);
        user.setAccessRejectionReason(null);
    }

    private static String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AdminUserAccessResponse toResponse(User user) {
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.getId()).isPresent();
        return new AdminUserAccessResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isLoginEnabled(),
                user.getLoginEnabledAt(),
                user.getCreatedAt(),
                hasProfile,
                user.getRegistrationSource(),
                user.getAcquisitionCampaign(),
                user.getContactPhone()
        );
    }

    private void requireAdmin() {
        if (!authorizationService.hasRole(UserRole.ADMIN)) {
            throw new BusinessException("Acesso restrito a administradores.");
        }
    }
}
