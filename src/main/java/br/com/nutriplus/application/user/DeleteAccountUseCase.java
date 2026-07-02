package br.com.nutriplus.application.user;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.DeleteAccountRequest;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.InvalidCredentialsException;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.service.AuditLogService;
import br.com.nutriplus.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeleteAccountUseCase {

    private final UserRepository userRepository;
    private final NutritionistRepository nutritionistRepository;
    private final CareRelationshipRepository careRelationshipRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final SubscriptionService subscriptionService;
    private final AuditLogService auditLogService;

    public DeleteAccountUseCase(UserRepository userRepository,
                                NutritionistRepository nutritionistRepository,
                                CareRelationshipRepository careRelationshipRepository,
                                PasswordHasherPort passwordHasherPort,
                                SubscriptionService subscriptionService,
                                AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.nutritionistRepository = nutritionistRepository;
        this.careRelationshipRepository = careRelationshipRepository;
        this.passwordHasherPort = passwordHasherPort;
        this.subscriptionService = subscriptionService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void execute(User user, DeleteAccountRequest request) {
        ensureCanDelete(user);
        verifyPassword(user, request.currentPassword());
        verifyEmailConfirmation(user.getEmail(), request.emailConfirmation());
        disableAutoRenewSilently(user.getId());

        auditLogService.log("ACCOUNT_DELETE_REQUEST", "USER", user);
        userRepository.delete(user);
    }

    private void ensureCanDelete(User user) {
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException(
                    "Contas de administrador não podem ser excluídas por aqui. Fale com o suporte.");
        }
        if (user.getRole() == UserRole.NUTRITIONIST) {
            Nutritionist nutritionist = nutritionistRepository.findByUserId(user.getId()).orElse(null);
            if (nutritionist != null) {
                var activeCare = careRelationshipRepository.findByNutritionistIdAndStatusInOrderByUpdatedAtDesc(
                        nutritionist.getId(),
                        List.of(CareRelationshipStatus.ACTIVE));
                if (!activeCare.isEmpty()) {
                    throw new BusinessException(
                            "Encerre o atendimento aos pacientes antes de excluir a conta de nutricionista.");
                }
            }
        }
    }

    private void verifyPassword(User user, String currentPassword) {
        if (!passwordHasherPort.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Senha atual incorreta.");
        }
    }

    private void verifyEmailConfirmation(String email, String confirmation) {
        if (!email.trim().equalsIgnoreCase(confirmation.trim())) {
            throw new BusinessException("Digite seu e-mail exatamente como cadastrado para confirmar a exclusão.");
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
