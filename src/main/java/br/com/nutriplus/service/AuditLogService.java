package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.AuditLog;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.repository.AuditLogRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String action, String entityType, String entityId, User user, String payloadJson) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .payloadJson(payloadJson)
                .correlationId(MDC.get(CorrelationIdFilter.MDC_KEY))
                .build();
        auditLogRepository.save(log);
    }

    @Transactional
    public void log(String action, String entityType, User user) {
        log(action, entityType, user != null ? String.valueOf(user.getId()) : null, user, null);
    }
}
