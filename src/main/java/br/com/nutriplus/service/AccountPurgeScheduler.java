package br.com.nutriplus.service;

import br.com.nutriplus.application.user.PurgeFrozenAccountUseCase;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountPurgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(AccountPurgeScheduler.class);
    static final int PURGE_AFTER_DAYS = 90;

    private final UserRepository userRepository;
    private final PurgeFrozenAccountUseCase purgeFrozenAccountUseCase;

    public AccountPurgeScheduler(UserRepository userRepository,
                                 PurgeFrozenAccountUseCase purgeFrozenAccountUseCase) {
        this.userRepository = userRepository;
        this.purgeFrozenAccountUseCase = purgeFrozenAccountUseCase;
    }

    @Scheduled(cron = "${nutriplus.account-purge.cron:0 15 4 * * *}")
    public void purgeExpiredFrozenAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(PURGE_AFTER_DAYS);
        List<User> expired = userRepository.findByAccountFrozenAtBefore(cutoff);
        if (expired.isEmpty()) {
            return;
        }
        log.info("Iniciando purge de {} conta(s) congelada(s) há mais de {} dias", expired.size(), PURGE_AFTER_DAYS);
        for (User user : expired) {
            try {
                purgeFrozenAccountUseCase.execute(user);
            } catch (Exception e) {
                log.warn("Falha ao excluir conta congelada userId={}: {}", user.getId(), e.getMessage());
            }
        }
    }
}
