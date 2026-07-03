package br.com.nutriplus.application.user;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurgeFrozenAccountUseCase {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PurgeFrozenAccountUseCase(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void execute(User user) {
        if (user.getAccountFrozenAt() == null) {
            return;
        }
        auditLogService.log("ACCOUNT_PURGED_AFTER_FREEZE", "USER", user);
        userRepository.delete(user);
    }
}
