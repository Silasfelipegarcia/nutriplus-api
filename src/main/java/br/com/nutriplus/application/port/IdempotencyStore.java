package br.com.nutriplus.application.port;

import br.com.nutriplus.domain.enums.IdempotencyStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyStore {

    Optional<StoredRecord> find(String idempotencyKey, long scopeUserId, String httpMethod, String requestPath);

    void saveInProgress(String idempotencyKey,
                        long scopeUserId,
                        String httpMethod,
                        String requestPath,
                        String requestHash,
                        LocalDateTime expiresAt);

    void markCompleted(String idempotencyKey,
                       long scopeUserId,
                       String httpMethod,
                       String requestPath,
                       int responseStatus,
                       String responseBody,
                       String responseContentType);

    void delete(String idempotencyKey, long scopeUserId, String httpMethod, String requestPath);

    int purgeExpired(LocalDateTime cutoff);

    record StoredRecord(
            IdempotencyStatus status,
            String requestHash,
            int responseStatus,
            String responseBody,
            String responseContentType,
            LocalDateTime createdAt
    ) {
    }
}
