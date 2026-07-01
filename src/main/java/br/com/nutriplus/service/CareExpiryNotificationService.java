package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.CareRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CareExpiryNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CareExpiryNotificationService.class);

    private final EmailSender emailService;

    public CareExpiryNotificationService(EmailSender emailService) {
        this.emailService = emailService;
    }

    public void notifyCareExpired(CareRelationship care) {
        String nutritionistName = care.getNutritionist().getUser().getName();
        String patientEmail = care.getPatient().getEmail();
        String patientName = care.getPatient().getName();
        log.info("Care relationship {} expired for patient {}", care.getId(), patientEmail);
        emailService.sendCareExpiredRatingPrompt(patientEmail, patientName, nutritionistName);
    }
}
