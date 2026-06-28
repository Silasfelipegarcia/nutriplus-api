package br.com.nutriplus.payment;

import br.com.nutriplus.infrastructure.config.MercadoPagoProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MercadoPagoWebhookVerifier {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookVerifier.class);

    private final MercadoPagoProperties properties;
    private final Environment environment;

    public MercadoPagoWebhookVerifier(MercadoPagoProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    public boolean verificar(HttpServletRequest request, String dataId) {
        String secret = properties.webhookSecret();
        if (secret == null || secret.isBlank()) {
            if (isProduction()) {
                log.error("Webhook MP rejeitado: MERCADOPAGO_WEBHOOK_SECRET ausente em produção");
                return false;
            }
            log.warn("Webhook MP aceito sem assinatura (dev/local)");
            return true;
        }

        String signature = request.getHeader("x-signature");
        String requestId = request.getHeader("x-request-id");
        if (signature == null || requestId == null || dataId == null || dataId.isBlank()) {
            log.warn("Webhook MP rejeitado: headers ou data.id ausentes");
            return false;
        }

        Map<String, String> parts = Arrays.stream(signature.split(","))
                .map(String::trim)
                .filter(s -> s.contains("="))
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1], (a, b) -> b));

        String ts = parts.get("ts");
        String v1 = parts.get("v1");
        if (ts == null || v1 == null) {
            log.warn("Webhook MP rejeitado: assinatura malformada");
            return false;
        }

        String manifest = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";
        String expected = hmacSha256Hex(secret, manifest);
        boolean valido = expected.equalsIgnoreCase(v1);
        if (!valido) {
            log.warn("Webhook MP rejeitado: assinatura inválida para data.id={}", dataId);
        }
        return valido;
    }

    private boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    private static String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao validar assinatura do webhook", e);
        }
    }
}
