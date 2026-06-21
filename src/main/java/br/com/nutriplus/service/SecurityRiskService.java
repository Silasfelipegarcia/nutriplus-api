package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.SecurityEvent;
import br.com.nutriplus.repository.SecurityEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class SecurityRiskService {

    private static final Logger log = LoggerFactory.getLogger(SecurityRiskService.class);

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            "(?i)(ignore\\s+(all\\s+)?previous|system\\s+prompt|jailbreak|bypass\\s+rules|act\\s+as\\s+admin)"
    );

    private static final int BLOCK_THRESHOLD = 80;
    private static final int THROTTLE_THRESHOLD = 50;

    private final SecurityEventRepository securityEventRepository;
    private final MeterRegistry meterRegistry;

    public SecurityRiskService(SecurityEventRepository securityEventRepository, MeterRegistry meterRegistry) {
        this.securityEventRepository = securityEventRepository;
        this.meterRegistry = meterRegistry;
    }

    public RiskDecision evaluate(RiskContext context) {
        int score = 0;
        StringBuilder details = new StringBuilder();

        if (context.authFailure()) {
            score += 25;
            details.append("auth_failure;");
        }
        if (context.rateLimitHit()) {
            score += 30;
            details.append("rate_limit;");
        }
        if (context.expensiveEndpoint()) {
            score += 10;
            details.append("expensive_endpoint;");
        }
        if (context.requestBody() != null && INJECTION_PATTERN.matcher(context.requestBody()).find()) {
            score += 40;
            details.append("injection_pattern;");
        }

        RiskAction action = RiskAction.ALLOW;
        if (score >= BLOCK_THRESHOLD) {
            action = RiskAction.BLOCK;
        } else if (score >= THROTTLE_THRESHOLD) {
            action = RiskAction.THROTTLE;
        }

        persistEvent(context, score, action, details.toString());
        meterRegistry.counter("nutriplus.security.risk.evaluations",
                "action", action.name().toLowerCase()).increment();

        if (action != RiskAction.ALLOW) {
            log.warn("Security risk {} score={} ip={} user={} path={}",
                    action, score, context.clientIp(), context.userId(), context.path());
        }

        return new RiskDecision(action, score);
    }

    @Transactional
    void persistEvent(RiskContext context, int score, RiskAction action, String details) {
        if (score < THROTTLE_THRESHOLD && !context.rateLimitHit()) {
            return;
        }
        securityEventRepository.save(SecurityEvent.builder()
                .userId(context.userId())
                .clientIp(context.clientIp())
                .action(context.path())
                .score(score)
                .blocked(action == RiskAction.BLOCK)
                .details(details.isBlank() ? null : details)
                .correlationId(context.correlationId())
                .build());
    }

    public enum RiskAction {
        ALLOW, THROTTLE, BLOCK
    }

    public record RiskContext(
            String path,
            String clientIp,
            Long userId,
            String correlationId,
            String requestBody,
            boolean authFailure,
            boolean rateLimitHit,
            boolean expensiveEndpoint
    ) {
    }

    public record RiskDecision(RiskAction action, int score) {
    }
}
