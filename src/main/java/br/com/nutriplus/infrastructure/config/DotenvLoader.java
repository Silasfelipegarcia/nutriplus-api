package br.com.nutriplus.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DotenvLoader {

    private static final Logger log = LoggerFactory.getLogger(DotenvLoader.class);
    private static final String PROPERTY_SOURCE_NAME = "nutriplusDotenv";

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("MERCADOPAGO_ACCESS_TOKEN", "nutriplus.mercadopago.access-token"),
            Map.entry("MERCADOPAGO_PUBLIC_KEY", "nutriplus.mercadopago.public-key"),
            Map.entry("MERCADOPAGO_WEBHOOK_SECRET", "nutriplus.mercadopago.webhook-secret"),
            Map.entry("MERCADOPAGO_MOCK_MODE", "nutriplus.mercadopago.mock-mode"),
            Map.entry("MERCADOPAGO_SANDBOX", "nutriplus.mercadopago.sandbox-mode"),
            Map.entry("MERCADOPAGO_API_BASE_URL", "nutriplus.mercadopago.api-base-url"),
            Map.entry("MERCADOPAGO_ATHLETE_MONTHLY_PRICE_CENTS", "nutriplus.mercadopago.athlete-monthly-price-cents"),
            Map.entry("MERCADOPAGO_ATHLETE_YEARLY_PRICE_CENTS", "nutriplus.mercadopago.athlete-yearly-price-cents"),
            Map.entry("FRONTEND_URL", "nutriplus.mercadopago.frontend-url"),
            Map.entry("API_PUBLIC_URL", "nutriplus.mercadopago.api-public-url")
    );

    private DotenvLoader() {
    }

    public static void loadInto(ConfigurableEnvironment environment) {
        Path envFile = Path.of(System.getProperty("user.dir", "."), ".env");
        if (!Files.isRegularFile(envFile)) {
            log.debug(".env não encontrado em {}", envFile.toAbsolutePath());
            return;
        }

        Map<String, Object> values = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if (key.isEmpty()) {
                    continue;
                }
                if (environment.getProperty(key) == null) {
                    values.put(key, value);
                }
                String alias = ALIASES.get(key);
                if (alias != null && environment.getProperty(alias) == null) {
                    values.put(alias, value);
                }
            }
        } catch (IOException e) {
            log.warn("Falha ao ler {}: {}", envFile, e.getMessage());
            return;
        }

        if (!values.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
            log.info("Carregadas {} variáveis de {}", values.size(), envFile.toAbsolutePath());
        }
    }
}
