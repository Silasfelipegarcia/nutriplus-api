package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.CareRelationship;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.repository.CareRelationshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CareExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(CareExpiryScheduler.class);

    private final CareRelationshipRepository careRelationshipRepository;
    private final CareExpiryNotificationService notificationService;

    public CareExpiryScheduler(CareRelationshipRepository careRelationshipRepository,
                               CareExpiryNotificationService notificationService) {
        this.careRelationshipRepository = careRelationshipRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void expireActiveCareRelationships() {
        LocalDateTime now = LocalDateTime.now();
        List<CareRelationship> expired = careRelationshipRepository
                .findByStatusAndExpiresAtBefore(CareRelationshipStatus.ACTIVE, now);
        if (expired.isEmpty()) {
            return;
        }
        log.info("Expirando {} vínculo(s) de acompanhamento", expired.size());
        for (CareRelationship care : expired) {
            care.setStatus(CareRelationshipStatus.EXPIRED);
            careRelationshipRepository.save(care);
            notificationService.notifyCareExpired(care);
        }
    }
}
