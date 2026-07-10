package br.com.nutriplus.application.user;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.service.AuditLogService;
import br.com.nutriplus.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminDeleteUserUseCase {

    private final UserRepository userRepository;
    private final NutritionistRepository nutritionistRepository;
    private final CareRelationshipRepository careRelationshipRepository;
    private final SubscriptionService subscriptionService;
    private final AuditLogService auditLogService;

    public AdminDeleteUserUseCase(UserRepository userRepository,
                                  NutritionistRepository nutritionistRepository,
                                  CareRelationshipRepository careRelationshipRepository,
                                  SubscriptionService subscriptionService,
                                  AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.nutritionistRepository = nutritionistRepository;
        this.careRelationshipRepository = careRelationshipRepository;
        this.subscriptionService = subscriptionService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void execute(User target, Long actingAdminId) {
        if (target.getId().equals(actingAdminId)) {
            throw new BusinessException("Você não pode excluir sua própria conta por aqui.");
        }
        if (target.getRole() == UserRole.ADMIN) {
            throw new BusinessException("Contas de administrador não podem ser excluídas por aqui.");
        }
        ensureNoActiveCareRelationships(target);
        disableAutoRenewSilently(target.getId());

        auditLogService.log("ADMIN_USER_DELETE", "USER", target);
        userRepository.delete(target);
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private void ensureNoActiveCareRelationships(User user) {
        if (user.getRole() != UserRole.NUTRITIONIST) {
            return;
        }
        Nutritionist nutritionist = nutritionistRepository.findByUserId(user.getId()).orElse(null);
        if (nutritionist == null) {
            return;
        }
        var activeCare = careRelationshipRepository.findByNutritionistIdAndStatusInOrderByUpdatedAtDesc(
                nutritionist.getId(),
                List.of(CareRelationshipStatus.ACTIVE));
        if (!activeCare.isEmpty()) {
            throw new BusinessException(
                    "Encerre o atendimento aos pacientes antes de excluir este nutricionista.");
        }
    }

    private void disableAutoRenewSilently(Long userId) {
        try {
            subscriptionService.cancelar(userId);
        } catch (RuntimeException ignored) {
            // Sem assinatura ativa ou renovação já desativada.
        }
    }
}
