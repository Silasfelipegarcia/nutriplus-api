package br.com.nutriplus.infrastructure.web;

import br.com.nutriplus.application.port.IdempotencyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class IdempotencyPurgeJob {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyPurgeJob.class);

    private final IdempotencyStore idempotencyStore;

    public IdempotencyPurgeJob(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void purgeExpired() {
        int removed = idempotencyStore.purgeExpired(LocalDateTime.now());
        if (removed > 0) {
            log.info("Idempotency purge removed {} expired records", removed);
        }
    }
}
