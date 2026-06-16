package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.AiRequestLog;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.AiRequestStatus;
import br.com.nutriplus.domain.enums.AiRequestType;
import br.com.nutriplus.repository.AiRequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiRequestLogService {

    private final AiRequestLogRepository aiRequestLogRepository;

    @Transactional
    public void log(User user, AiRequestType type, String requestPayload, String responsePayload,
                    AiRequestStatus status, String errorMessage, Integer durationMs) {
        AiRequestLog log = AiRequestLog.builder()
                .user(user)
                .requestType(type)
                .requestPayload(requestPayload)
                .responsePayload(responsePayload)
                .status(status)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .build();
        aiRequestLogRepository.save(log);
    }
}
