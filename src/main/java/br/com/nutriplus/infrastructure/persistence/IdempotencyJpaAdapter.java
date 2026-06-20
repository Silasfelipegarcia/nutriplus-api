package br.com.nutriplus.infrastructure.persistence;

import br.com.nutriplus.application.port.IdempotencyStore;
import br.com.nutriplus.domain.entity.IdempotencyRecord;
import br.com.nutriplus.domain.enums.IdempotencyStatus;
import br.com.nutriplus.repository.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class IdempotencyJpaAdapter implements IdempotencyStore {

    private final IdempotencyRecordRepository repository;

    public IdempotencyJpaAdapter(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredRecord> find(String idempotencyKey, long scopeUserId, String httpMethod, String requestPath) {
        return repository.findByIdempotencyKeyAndScopeUserIdAndHttpMethodAndRequestPath(
                        idempotencyKey, scopeUserId, httpMethod, requestPath)
                .map(this::toStored);
    }

    @Override
    @Transactional
    public void saveInProgress(String idempotencyKey,
                               long scopeUserId,
                               String httpMethod,
                               String requestPath,
                               String requestHash,
                               LocalDateTime expiresAt) {
        try {
            repository.save(IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .scopeUserId(scopeUserId)
                    .httpMethod(httpMethod)
                    .requestPath(requestPath)
                    .requestHash(requestHash)
                    .status(IdempotencyStatus.IN_PROGRESS)
                    .expiresAt(expiresAt)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            throw ex;
        }
    }

    @Override
    @Transactional
    public void markCompleted(String idempotencyKey,
                              long scopeUserId,
                              String httpMethod,
                              String requestPath,
                              int responseStatus,
                              String responseBody,
                              String responseContentType) {
        IdempotencyRecord record = repository
                .findByIdempotencyKeyAndScopeUserIdAndHttpMethodAndRequestPath(
                        idempotencyKey, scopeUserId, httpMethod, requestPath)
                .orElseThrow(() -> new IllegalStateException("Registro de idempotência não encontrado"));
        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setResponseStatus(responseStatus);
        record.setResponseBody(responseBody);
        record.setResponseContentType(responseContentType);
        repository.save(record);
    }

    @Override
    @Transactional
    public void delete(String idempotencyKey, long scopeUserId, String httpMethod, String requestPath) {
        repository.findByIdempotencyKeyAndScopeUserIdAndHttpMethodAndRequestPath(
                        idempotencyKey, scopeUserId, httpMethod, requestPath)
                .ifPresent(repository::delete);
    }

    @Override
    @Transactional
    public int purgeExpired(LocalDateTime cutoff) {
        return repository.deleteExpired(cutoff);
    }

    private StoredRecord toStored(IdempotencyRecord record) {
        return new StoredRecord(
                record.getStatus(),
                record.getRequestHash(),
                record.getResponseStatus() != null ? record.getResponseStatus() : 0,
                record.getResponseBody(),
                record.getResponseContentType(),
                record.getCreatedAt()
        );
    }
}
