package br.com.nutriplus.subscription;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.service.TrialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class TrialExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrialExpirationScheduler.class);

    private final UserRepository userRepository;
    private final TrialService trialService;

    public TrialExpirationScheduler(UserRepository userRepository, TrialService trialService) {
        this.userRepository = userRepository;
        this.trialService = trialService;
    }

    @Scheduled(cron = "0 30 6 * * *")
    @Transactional
    public void processarTrialsExpirados() {
        List<User> candidatos = userRepository.findTrialsExpirados(Instant.now());
        if (candidatos.isEmpty()) {
            return;
        }
        log.info("Processando {} trial(s) expirado(s)", candidatos.size());
        for (User user : candidatos) {
            trialService.expirarTrialSeNecessario(user);
        }
    }
}
